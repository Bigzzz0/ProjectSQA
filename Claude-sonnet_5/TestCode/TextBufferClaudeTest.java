package com.fasterxml.jackson.core.util;

import java.math.BigDecimal;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TextBufferClaudeTest {

    /**
     * @target TextBuffer#contentsAsDecimal()
     * @scenario Buffer populated with "NaN" content, contentsAsDecimal() called
     * @defectRisk Known defect where NumberFormatException message is null instead of
     *             containing "can not be represented as BigDecimal"
     */
    @Test(timeout = 4000)
    public void testContentsAsDecimal_NaN_DescriptiveMessage_Issue98() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.resetWithCopy("NaN".toCharArray(), 0, 3);
        try {
            tb.contentsAsDecimal();
            fail("Expected NumberFormatException for NaN input");
        } catch (NumberFormatException e) {
            String msg = e.getMessage();
            assertNotNull("Exception message should not be null", msg);
            assertTrue("Expected message to contain 'can not be represented as BigDecimal' but was: " + msg,
                    msg.contains("can not be represented as BigDecimal"));
        }
    }

    /**
     * @target TextBuffer#TextBuffer(BufferRecycler)
     * @scenario Constructing buffer with a valid BufferRecycler allocator
     * @defectRisk Constructor failing to store allocator reference properly
     */
    @Test(timeout = 4000)
    public void testConstructorWithAllocator() {
        BufferRecycler recycler = new BufferRecycler();
        TextBuffer tb = new TextBuffer(recycler);
        assertNotNull(tb);
        assertEquals(0, tb.size());
    }

    /**
     * @target TextBuffer#TextBuffer(BufferRecycler)
     * @scenario Constructing buffer with null allocator (no recycler)
     * @defectRisk NPE if allocator usage isn't null-checked
     */
    @Test(timeout = 4000)
    public void testConstructorWithNullAllocator() {
        TextBuffer tb = new TextBuffer(null);
        assertNotNull(tb);
        assertEquals(0, tb.size());
    }

    /**
     * @target TextBuffer#resetWithEmpty()
     * @scenario Reset buffer to empty state after having content
     * @defectRisk Failure to properly clear segments/size on reset
     */
    @Test(timeout = 4000)
    public void testResetWithEmpty() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append('a');
        tb.append('b');
        assertEquals(2, tb.size());
        tb.resetWithEmpty();
        assertEquals(0, tb.size());
        assertEquals("", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#resetWithEmpty()
     * @scenario Reset buffer to empty state after using segments (multi-segment)
     * @defectRisk clearSegments() not resetting _segmentSize/_currentSize correctly
     */
    @Test(timeout = 4000)
    public void testResetWithEmptyAfterSegments() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] big = new char[3000];
        Arrays.fill(big, 'x');
        tb.append(big, 0, big.length);
        assertTrue(tb.size() > 0);
        tb.resetWithEmpty();
        assertEquals(0, tb.size());
    }

    /**
     * @target TextBuffer#resetWithShared(char[], int, int)
     * @scenario Reset with a shared buffer, non-zero start offset
     * @defectRisk Incorrect handling of _inputStart/_inputLen leading to wrong size()
     */
    @Test(timeout = 4000)
    public void testResetWithShared() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] src = "HelloWorld".toCharArray();
        tb.resetWithShared(src, 2, 5); // "lloWo"
        assertEquals(5, tb.size());
        assertEquals(2, tb.getTextOffset());
        assertTrue(tb.hasTextAsCharacters());
        assertEquals("lloWo", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#resetWithShared(char[], int, int)
     * @scenario Reset with shared buffer after having segments already populated
     * @defectRisk clearSegments not invoked properly, leftover segment data
     */
    @Test(timeout = 4000)
    public void testResetWithSharedAfterSegments() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] big = new char[3000];
        Arrays.fill(big, 'y');
        tb.append(big, 0, big.length);
        char[] src = "abcdef".toCharArray();
        tb.resetWithShared(src, 1, 3); // "bcd"
        assertEquals(3, tb.size());
        assertEquals("bcd", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#resetWithCopy(char[], int, int)
     * @scenario Reset with copy of char array data, fresh buffer (no current segment yet)
     * @defectRisk findBuffer not called correctly when _currentSegment is null
     */
    @Test(timeout = 4000)
    public void testResetWithCopyFreshBuffer() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] src = "TestData".toCharArray();
        tb.resetWithCopy(src, 0, src.length);
        assertEquals(src.length, tb.size());
        assertEquals("TestData", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#resetWithCopy(char[], int, int)
     * @scenario Reset with copy of char array data, when buffer already has segments
     * @defectRisk clearSegments not properly clearing before copy append
     */
    @Test(timeout = 4000)
    public void testResetWithCopyAfterSegments() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] big = new char[3000];
        Arrays.fill(big, 'z');
        tb.append(big, 0, big.length);
        char[] src = "NewContent".toCharArray();
        tb.resetWithCopy(src, 0, src.length);
        assertEquals(src.length, tb.size());
        assertEquals("NewContent", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#resetWithString(String)
     * @scenario Reset buffer directly with a String value
     * @defectRisk _resultString not stored/retrieved correctly
     */
    @Test(timeout = 4000)
    public void testResetWithString() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.resetWithString("HelloString");
        assertEquals(11, tb.size());
        assertEquals("HelloString", tb.contentsAsString());
        assertFalse(tb.hasTextAsCharacters());
    }

    /**
     * @target TextBuffer#append(char)
     * @scenario Appending single characters within a fresh buffer's initial segment
     * @defectRisk Off-by-one error in _currentSize/curr.length comparison
     */
    @Test(timeout = 4000)
    public void testAppendSingleChar() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append('H');
        tb.append('i');
        assertEquals(2, tb.size());
        assertEquals("Hi", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#append(char)
     * @scenario Appending single char when buffer was in shared mode (triggers unshare)
     * @defectRisk unshare() not correctly copying shared content before append
     */
    @Test(timeout = 4000)
    public void testAppendCharAfterShared() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] src = "Shared".toCharArray();
        tb.resetWithShared(src, 0, 6);
        tb.append('!');
        assertEquals("Shared!", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#append(char)
     * @scenario Appending characters enough to force segment expansion
     * @defectRisk expand() incorrectly sizing new segment, data loss across segments
     */
    @Test(timeout = 4000)
    public void testAppendCharCausesExpansion() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        StringBuilder expected = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            char c = (char) ('a' + (i % 26));
            tb.append(c);
            expected.append(c);
        }
        assertEquals(expected.length(), tb.size());
        assertEquals(expected.toString(), tb.contentsAsString());
    }

    /**
     * @target TextBuffer#append(char[], int, int)
     * @scenario Appending char array fully fits in current segment
     * @defectRisk Incorrect max calculation causing wrong copy length
     */
    @Test(timeout = 4000)
    public void testAppendCharArrayFits() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] data = "SimpleAppend".toCharArray();
        tb.append(data, 0, data.length);
        assertEquals("SimpleAppend", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#append(char[], int, int)
     * @scenario Appending char array that spans multiple segments (forces expand loop)
     * @defectRisk do-while loop in append(char[],int,int) mishandling boundary/leftover length
     */
    @Test(timeout = 4000)
    public void testAppendCharArrayMultiSegment() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] data = new char[5000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (char) ('A' + (i % 26));
        }
        tb.append(data, 0, data.length);
        assertEquals(data.length, tb.size());
        assertEquals(new String(data), tb.contentsAsString());
    }

    /**
     * @target TextBuffer#append(char[], int, int)
     * @scenario Appending char array while buffer currently holds shared data
     * @defectRisk unshare(len) called with wrong needExtra causing overflow/loss
     */
    @Test(timeout = 4000)
    public void testAppendCharArrayAfterShared() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] shared = "Base".toCharArray();
        tb.resetWithShared(shared, 0, 4);
        char[] extra = "Extra".toCharArray();
        tb.append(extra, 0, extra.length);
        assertEquals("BaseExtra", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#append(String, int, int)
     * @scenario Appending substring of a String that fits within current segment
     * @defectRisk getChars offset/length miscalculation
     */
    @Test(timeout = 4000)
    public void testAppendStringFits() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        String s = "HelloWorldTest";
        tb.append(s, 0, s.length());
        assertEquals(s, tb.contentsAsString());
    }

    /**
     * @target TextBuffer#append(String, int, int)
     * @scenario Appending a substring segment (partial range) of a larger String
     * @defectRisk Incorrect offset arithmetic for substring extraction
     */
    @Test(timeout = 4000)
    public void testAppendStringPartial() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        String s = "HelloWorldTest";
        tb.append(s, 5, 5); // "World"
        assertEquals("World", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#append(String, int, int)
     * @scenario Appending long string that spans multiple segments (do-while loop)
     * @defectRisk expand() sizing / do-while loop leftover length miscalculation for Strings
     */
    @Test(timeout = 4000)
    public void testAppendStringMultiSegment() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        String s = sb.toString();
        tb.append(s, 0, s.length());
        assertEquals(s, tb.contentsAsString());
    }

    /**
     * @target TextBuffer#append(String, int, int)
     * @scenario Appending String while buffer is currently shared (forces unshare)
     * @defectRisk unshare(len) incorrectly sizing buffer for subsequent String append
     */
    @Test(timeout = 4000)
    public void testAppendStringAfterShared() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] shared = "Pre".toCharArray();
        tb.resetWithShared(shared, 0, 3);
        tb.append("Post", 0, 4);
        assertEquals("PrePost", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#contentsAsString()
     * @scenario Content empty, single segment, no data appended
     * @defectRisk Wrong branch chosen for empty buffer (currLen==0 case)
     */
    @Test(timeout = 4000)
    public void testContentsAsStringEmptySingleSegment() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.getCurrentSegment(); // ensures _currentSegment allocated but no data
        assertEquals("", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#contentsAsString()
     * @scenario Content spread across multiple segments requiring StringBuilder aggregation
     * @defectRisk Segment iteration in contentsAsString mismatched lengths / missing data
     */
    @Test(timeout = 4000)
    public void testContentsAsStringMultiSegment() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] data = new char[4000];
        Arrays.fill(data, 'q');
        tb.append(data, 0, data.length);
        String result = tb.contentsAsString();
        assertEquals(4000, result.length());
        for (int i = 0; i < result.length(); i++) {
            assertEquals('q', result.charAt(i));
        }
    }

    /**
     * @target TextBuffer#contentsAsString()
     * @scenario Content from shared input buffer with zero length
     * @defectRisk _inputLen < 1 branch returning wrong non-empty string
     */
    @Test(timeout = 4000)
    public void testContentsAsStringSharedZeroLength() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] src = "abc".toCharArray();
        tb.resetWithShared(src, 0, 0);
        assertEquals("", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#contentsAsString()
     * @scenario Cached _resultArray already set, then contentsAsString requested (shortcut path)
     * @defectRisk _resultArray shortcut path not producing correct String
     */
    @Test(timeout = 4000)
    public void testContentsAsStringFromResultArrayShortcut() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append("CachedArrayTest", 0, "CachedArrayTest".length());
        char[] arr = tb.contentsAsArray(); // sets _resultArray
        assertNotNull(arr);
        String s = tb.contentsAsString();
        assertEquals("CachedArrayTest", s);
    }

    /**
     * @target TextBuffer#contentsAsArray()
     * @scenario Single segment content converted to array
     * @defectRisk buildResultArray incorrect size/offset for single segment
     */
    @Test(timeout = 4000)
    public void testContentsAsArraySingleSegment() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append("ArrayTest", 0, 9);
        char[] arr = tb.contentsAsArray();
        assertEquals("ArrayTest", new String(arr));
    }

    /**
     * @target TextBuffer#contentsAsArray()
     * @scenario Multi-segment content converted to array (aggregation loop)
     * @defectRisk Offset accumulation across segments incorrect in buildResultArray
     */
    @Test(timeout = 4000)
    public void testContentsAsArrayMultiSegment() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] data = new char[4500];
        for (int i = 0; i < data.length; i++) {
            data[i] = (char) ('0' + (i % 10));
        }
        tb.append(data, 0, data.length);
        char[] result = tb.contentsAsArray();
        assertEquals(data.length, result.length);
        assertArrayEquals(data, result);
    }

    /**
     * @target TextBuffer#contentsAsArray()
     * @scenario Empty buffer, size()==0, should return NO_CHARS
     * @defectRisk buildResultArray not handling size<1 properly
     */
    @Test(timeout = 4000)
    public void testContentsAsArrayEmpty() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] result = tb.contentsAsArray();
        assertEquals(0, result.length);
    }

    /**
     * @target TextBuffer#contentsAsArray()
     * @scenario Shared buffer content with non-zero start offset converted to array
     * @defectRisk Arrays.copyOfRange used with wrong start/end bounds
     */
    @Test(timeout = 4000)
    public void testContentsAsArraySharedNonZeroStart() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] src = "0123456789".toCharArray();
        tb.resetWithShared(src, 3, 4); // "3456"
        char[] result = tb.contentsAsArray();
        assertEquals("3456", new String(result));
    }

    /**
     * @target TextBuffer#contentsAsArray()
     * @scenario Shared buffer content with zero start offset (Arrays.copyOf branch)
     * @defectRisk start==0 branch mis-copying length
     */
    @Test(timeout = 4000)
    public void testContentsAsArraySharedZeroStart() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] src = "abcdef".toCharArray();
        tb.resetWithShared(src, 0, 6);
        char[] result = tb.contentsAsArray();
        assertEquals("abcdef", new String(result));
    }

    /**
     * @target TextBuffer#contentsAsArray()
     * @scenario Shared buffer with zero length returns NO_CHARS
     * @defectRisk len<1 branch in buildResultArray for shared buffer case
     */
    @Test(timeout = 4000)
    public void testContentsAsArraySharedZeroLen() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] src = "abcdef".toCharArray();
        tb.resetWithShared(src, 0, 0);
        char[] result = tb.contentsAsArray();
        assertEquals(0, result.length);
    }

    /**
     * @target TextBuffer#contentsAsArray()
     * @scenario Value set via resetWithString, buildResultArray shortcut via _resultString
     * @defectRisk _resultString.toCharArray() shortcut path bug
     */
    @Test(timeout = 4000)
    public void testContentsAsArrayFromResultStringShortcut() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.resetWithString("StringShortcut");
        char[] result = tb.contentsAsArray();
        assertEquals("StringShortcut", new String(result));
    }

    /**
     * @target TextBuffer#contentsAsDouble()
     * @scenario Valid numeric content converted to double
     * @defectRisk contentsAsString()/parseDouble integration errors
     */
    @Test(timeout = 4000)
    public void testContentsAsDoubleValid() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append("3.14159", 0, 7);
        double d = tb.contentsAsDouble();
        assertEquals(3.14159, d, 0.00001);
    }

    /**
     * @target TextBuffer#contentsAsDouble()
     * @scenario Invalid numeric content throws NumberFormatException
     * @defectRisk parseDouble not throwing expected exception type
     */
    @Test(timeout = 4000)
    public void testContentsAsDoubleInvalid() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append("notANumber", 0, 10);
        try {
            tb.contentsAsDouble();
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    /**
     * @target TextBuffer#contentsAsDecimal()
     * @scenario Valid decimal content, single segment path (_segmentSize == 0)
     * @defectRisk NumberInput.parseBigDecimal(char[],int,int) misuse
     */
    @Test(timeout = 4000)
    public void testContentsAsDecimalSingleSegment() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append("123.456", 0, 7);
        BigDecimal bd = tb.contentsAsDecimal();
        assertEquals(new BigDecimal("123.456"), bd);
    }

    /**
     * @target TextBuffer#contentsAsDecimal()
     * @scenario Valid decimal content from shared buffer directly
     * @defectRisk _inputStart>=0 branch parseBigDecimal with wrong offset/len
     */
    @Test(timeout = 4000)
    public void testContentsAsDecimalSharedBuffer() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] src = "  42.5  ".toCharArray();
        tb.resetWithShared(src, 2, 4); // "42.5"
        BigDecimal bd = tb.contentsAsDecimal();
        assertEquals(new BigDecimal("42.5"), bd);
    }

    /**
     * @target TextBuffer#contentsAsDecimal()
     * @scenario Valid decimal content requiring multi-segment aggregation via contentsAsArray()
     * @defectRisk _segmentSize != 0 branch calling contentsAsArray() incorrectly
     */
    @Test(timeout = 4000)
    public void testContentsAsDecimalMultiSegment() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        // Force expansion by appending a large number of digits then a decimal
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append('1');
        }
        String numStr = sb.toString();
        tb.append(numStr, 0, numStr.length());
        BigDecimal bd = tb.contentsAsDecimal();
        assertEquals(new BigDecimal(numStr), bd);
    }

    /**
     * @target TextBuffer#contentsAsDecimal()
     * @scenario Already-cached _resultArray used as shortcut for decimal parse
     * @defectRisk _resultArray != null branch bypassing correct parse path
     */
    @Test(timeout = 4000)
    public void testContentsAsDecimalFromCachedResultArray() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append("99.99", 0, 5);
        tb.contentsAsArray(); // populate _resultArray
        BigDecimal bd = tb.contentsAsDecimal();
        assertEquals(new BigDecimal("99.99"), bd);
    }

    /**
     * @target TextBuffer#releaseBuffers()
     * @scenario Release buffers when allocator is null (falls back to resetWithEmpty)
     * @defectRisk NPE if allocator null-check missing
     */
    @Test(timeout = 4000)
    public void testReleaseBuffersNullAllocator() {
        TextBuffer tb = new TextBuffer(null);
        tb.append("SomeData", 0, 8);
        tb.releaseBuffers();
        assertEquals(0, tb.size());
    }

    /**
     * @target TextBuffer#releaseBuffers()
     * @scenario Release buffers with valid allocator and existing current segment
     * @defectRisk Allocator not receiving buffer back, or _currentSegment not nulled before reuse
     */
    @Test(timeout = 4000)
    public void testReleaseBuffersWithAllocator() {
        BufferRecycler recycler = new BufferRecycler();
        TextBuffer tb = new TextBuffer(recycler);
        tb.append("DataToRelease", 0, 13);
        tb.releaseBuffers();
        assertEquals(0, tb.size());
    }

    /**
     * @target TextBuffer#releaseBuffers()
     * @scenario Release buffers when _currentSegment is null (no-op branch)
     * @defectRisk NPE thrown when _currentSegment already null
     */
    @Test(timeout = 4000)
    public void testReleaseBuffersNoCurrentSegment() {
        BufferRecycler recycler = new BufferRecycler();
        TextBuffer tb = new TextBuffer(recycler);
        // Never touched _currentSegment
        tb.releaseBuffers();
        assertEquals(0, tb.size());
    }

    /**
     * @target TextBuffer#getTextBuffer()
     * @scenario Shared input buffer branch return
     * @defectRisk Wrong branch precedence returning incorrect buffer
     */
    @Test(timeout = 4000)
    public void testGetTextBufferShared() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] src = "GetBufferTest".toCharArray();
        tb.resetWithShared(src, 0, src.length);
        char[] buf = tb.getTextBuffer();
        assertSame(src, buf);
    }

    /**
     * @target TextBuffer#getTextBuffer()
     * @scenario Single-segment (non-shared) branch
     * @defectRisk Returning wrong array when no segments present
     */
    @Test(timeout = 4000)
    public void testGetTextBufferSingleSegment() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append("SegmentBuf", 0, 10);
        char[] buf = tb.getTextBuffer();
        assertNotNull(buf);
        assertEquals("SegmentBuf", new String(buf, 0, tb.getCurrentSegmentSize()));
    }

    /**
     * @target TextBuffer#getTextBuffer()
     * @scenario Multi-segment branch triggers contentsAsArray()
     * @defectRisk Segmented buffer not aggregated correctly through getTextBuffer
     */
    @Test(timeout = 4000)
    public void testGetTextBufferMultiSegment() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] data = new char[4000];
        Arrays.fill(data, 'm');
        tb.append(data, 0, data.length);
        char[] buf = tb.getTextBuffer();
        assertEquals(4000, buf.length);
    }

    /**
     * @target TextBuffer#getTextBuffer()
     * @scenario _resultString branch converting to char array
     * @defectRisk toCharArray shortcut not caching properly
     */
    @Test(timeout = 4000)
    public void testGetTextBufferFromResultString() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.resetWithString("ResultStringBuf");
        char[] buf = tb.getTextBuffer();
        assertEquals("ResultStringBuf", new String(buf));
    }

    /**
     * @target TextBuffer#hasTextAsCharacters()
     * @scenario False when _resultString set without array/shared
     * @defectRisk Incorrect boolean logic ordering
     */
    @Test(timeout = 4000)
    public void testHasTextAsCharactersFalseForString() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.resetWithString("justAString");
        assertFalse(tb.hasTextAsCharacters());
    }

    /**
     * @target TextBuffer#hasTextAsCharacters()
     * @scenario True for default segment-based buffer
     * @defectRisk Default true branch not reached
     */
    @Test(timeout = 4000)
    public void testHasTextAsCharactersTrueDefault() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append('x');
        assertTrue(tb.hasTextAsCharacters());
    }

    /**
     * @target TextBuffer#getCurrentSegment()
     * @scenario Called when _currentSegment is null - triggers findBuffer
     * @defectRisk findBuffer not invoked or wrong buffer size allocated
     */
    @Test(timeout = 4000)
    public void testGetCurrentSegmentAllocatesWhenNull() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] seg = tb.getCurrentSegment();
        assertNotNull(seg);
    }

    /**
     * @target TextBuffer#getCurrentSegment()
     * @scenario Called when shared buffer in use, triggers unshare
     * @defectRisk unshare(1) miscalculating needed extra room
     */
    @Test(timeout = 4000)
    public void testGetCurrentSegmentTriggersUnshare() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] src = "SharedData".toCharArray();
        tb.resetWithShared(src, 0, src.length);
        char[] seg = tb.getCurrentSegment();
        assertNotNull(seg);
        assertEquals(src.length, tb.getCurrentSegmentSize());
    }

    /**
     * @target TextBuffer#getCurrentSegment()
     * @scenario Called when current segment full, triggers expand
     * @defectRisk expand(1) not growing segment as expected
     */
    @Test(timeout = 4000)
    public void testGetCurrentSegmentTriggersExpand() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        // Fill exactly to capacity using a small forced buffer via resetWithCopy
        char[] data = new char[1000];
        Arrays.fill(data, 'k');
        tb.resetWithCopy(data, 0, data.length);
        char[] seg = tb.getCurrentSegment();
        assertNotNull(seg);
    }

    /**
     * @target TextBuffer#emptyAndGetCurrentSegment()
     * @scenario Resets state and returns fresh current segment for reuse
     * @defectRisk Segment not reset to zero size or old data leaking through
     */
    @Test(timeout = 4000)
    public void testEmptyAndGetCurrentSegment() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append("OldData", 0, 7);
        char[] seg = tb.emptyAndGetCurrentSegment();
        assertNotNull(seg);
        assertEquals(0, tb.getCurrentSegmentSize());
        assertEquals(0, tb.size());
    }

    /**
     * @target TextBuffer#getCurrentSegmentSize() and #setCurrentLength(int)
     * @scenario Manually setting current segment length and reading it back
     * @defectRisk setCurrentLength not correctly updating internal state used by size()
     */
    @Test(timeout = 4000)
    public void testSetAndGetCurrentSegmentSize() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] seg = tb.getCurrentSegment();
        seg[0] = 'A';
        seg[1] = 'B';
        tb.setCurrentLength(2);
        assertEquals(2, tb.getCurrentSegmentSize());
        assertEquals(2, tb.size());
        assertEquals("AB", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#finishCurrentSegment()
     * @scenario Explicitly finishing current segment and starting a new one
     * @defectRisk Segment list not updated correctly, or new segment sized incorrectly
     */
    @Test(timeout = 4000)
    public void testFinishCurrentSegment() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] seg = tb.getCurrentSegment();
        Arrays.fill(seg, 'z');
        tb.setCurrentLength(seg.length);
        char[] newSeg = tb.finishCurrentSegment();
        assertNotNull(newSeg);
        assertEquals(0, tb.getCurrentSegmentSize());
        assertTrue(tb.size() >= seg.length);
    }

    /**
     * @target TextBuffer#expandCurrentSegment()
     * @scenario Explicit expansion call growing current segment by 50%
     * @defectRisk Growth calculation incorrect, data not preserved after copyOf
     */
    @Test(timeout = 4000)
    public void testExpandCurrentSegment() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] seg = tb.getCurrentSegment();
        int originalLen = seg.length;
        seg[0] = 'X';
        char[] expanded = tb.expandCurrentSegment();
        assertTrue(expanded.length > originalLen);
        assertEquals('X', expanded[0]);
    }

    /**
     * @target TextBuffer#toString()
     * @scenario toString() delegates to contentsAsString()
     * @defectRisk toString not properly delegating, producing wrong content
     */
    @Test(timeout = 4000)
    public void testToStringDelegatesToContentsAsString() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append("ToStringTest", 0, 12);
        assertEquals("ToStringTest", tb.toString());
    }

    /**
     * @target TextBuffer#ensureNotShared()
     * @scenario Buffer in shared mode, ensureNotShared triggers unshare
     * @defectRisk unshare not invoked when _inputStart >= 0
     */
    @Test(timeout = 4000)
    public void testEnsureNotSharedWhenShared() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] src = "EnsureTest".toCharArray();
        tb.resetWithShared(src, 0, src.length);
        tb.ensureNotShared();
        // After unsharing, appending should work fine and preserve original content
        tb.append('!');
        assertEquals("EnsureTest!", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#ensureNotShared()
     * @scenario Buffer NOT in shared mode; method should be no-op
     * @defectRisk Unintended unshare triggered when not needed
     */
    @Test(timeout = 4000)
    public void testEnsureNotSharedWhenNotShared() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append("NotShared", 0, 9);
        tb.ensureNotShared();
        assertEquals("NotShared", tb.contentsAsString());
    }

    /**
     * @target TextBuffer#size()
     * @scenario size() when _resultArray already cached
     * @defectRisk _resultArray branch returning wrong length
     */
    @Test(timeout = 4000)
    public void testSizeFromResultArray() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append("SizeCheck", 0, 9);
        tb.contentsAsArray();
        assertEquals(9, tb.size());
    }

    /**
     * @target TextBuffer#size()
     * @scenario size() when _resultString already cached
     * @defectRisk _resultString branch returning wrong length
     */
    @Test(timeout = 4000)
    public void testSizeFromResultString() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.resetWithString("SizeFromString");
        assertEquals(14, tb.size());
    }

    /**
     * @target TextBuffer#getTextOffset()
     * @scenario getTextOffset() returns 0 for non-shared buffer
     * @defectRisk Wrong default offset returned
     */
    @Test(timeout = 4000)
    public void testGetTextOffsetNonShared() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append("OffsetTest", 0, 10);
        assertEquals(0, tb.getTextOffset());
    }

    /**
     * @target TextBuffer#append(char[], int, int)
     * @scenario Appending zero-length array segment (edge case, max >= len trivially true)
     * @defectRisk Zero-length append incorrectly modifying size or throwing
     */
    @Test(timeout = 4000)
    public void testAppendCharArrayZeroLength() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        char[] data = new char[0];
        tb.append(data, 0, 0);
        assertEquals(0, tb.size());
    }

    /**
     * @target TextBuffer#append(String, int, int)
     * @scenario Appending zero-length string range
     * @defectRisk Zero-length string append incorrectly modifying state
     */
    @Test(timeout = 4000)
    public void testAppendStringZeroLength() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append("anything", 0, 0);
        assertEquals(0, tb.size());
    }

    /**
     * @target TextBuffer#contentsAsDecimal()
     * @scenario Additional defect-focused check: NaN via append() rather than resetWithCopy
     * @defectRisk Ensures message content correctness across different population paths
     */
    @Test(timeout = 4000)
    public void testContentsAsDecimal_NaN_ViaAppend() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append("NaN", 0, 3);
        try {
            tb.contentsAsDecimal();
            fail("Expected NumberFormatException for NaN input via append");
        } catch (NumberFormatException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("can not be represented as BigDecimal"));
        }
    }
}