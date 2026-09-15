package com.fasterxml.jackson.core.util;

import java.math.BigDecimal;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TextBufferDeepseekTest {

    /**
     * @target contentsAsDecimal()
     * @scenario Populate buffer with "NaN" and call contentsAsDecimal()
     * @defectRisk Defective version returns null message instead of "can not be represented as BigDecimal"
     */
    @Test(timeout = 4000)
    public void testContentsAsDecimalWithNaN() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.resetWithCopy("NaN".toCharArray(), 0, 3);
        try {
            textBuffer.contentsAsDecimal();
            fail("Expected NumberFormatException");
        } catch (NumberFormatException e) {
            assertNotNull("Exception message should not be null", e.getMessage());
            assertTrue("Exception message should contain 'can not be represented as BigDecimal'",
                    e.getMessage().contains("can not be represented as BigDecimal"));
        }
    }

    /**
     * @target resetWithEmpty()
     * @scenario Reset buffer to empty state
     * @defectRisk Verify size is zero after reset
     */
    @Test(timeout = 4000)
    public void testResetWithEmpty() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.append("test", 0, 4);
        textBuffer.resetWithEmpty();
        assertEquals(0, textBuffer.size());
        assertEquals("", textBuffer.contentsAsString());
    }

    /**
     * @target resetWithShared()
     * @scenario Initialize with shared buffer
     * @defectRisk Verify shared buffer usage and size
     */
    @Test(timeout = 4000)
    public void testResetWithShared() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        char[] shared = "shared".toCharArray();
        textBuffer.resetWithShared(shared, 0, 6);
        assertEquals(6, textBuffer.size());
        assertEquals("shared", textBuffer.contentsAsString());
        assertTrue(textBuffer.hasTextAsCharacters());
    }

    /**
     * @target resetWithCopy()
     * @scenario Initialize with copied buffer
     * @defectRisk Verify copy behavior and size
     */
    @Test(timeout = 4000)
    public void testResetWithCopy() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        char[] data = "copy".toCharArray();
        textBuffer.resetWithCopy(data, 0, 4);
        assertEquals(4, textBuffer.size());
        assertEquals("copy", textBuffer.contentsAsString());
    }

    /**
     * @target append(char)
     * @scenario Append single characters
     * @defectRisk Verify character appending
     */
    @Test(timeout = 4000)
    public void testAppendChar() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.append('a');
        textBuffer.append('b');
        textBuffer.append('c');
        assertEquals(3, textBuffer.size());
        assertEquals("abc", textBuffer.contentsAsString());
    }

    /**
     * @target append(char[], int, int)
     * @scenario Append character array
     * @defectRisk Verify array appending
     */
    @Test(timeout = 4000)
    public void testAppendCharArray() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        char[] data = "hello".toCharArray();
        textBuffer.append(data, 0, 5);
        assertEquals(5, textBuffer.size());
        assertEquals("hello", textBuffer.contentsAsString());
    }

    /**
     * @target append(String, int, int)
     * @scenario Append string segment
     * @defectRisk Verify string appending
     */
    @Test(timeout = 4000)
    public void testAppendString() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.append("world", 0, 5);
        assertEquals(5, textBuffer.size());
        assertEquals("world", textBuffer.contentsAsString());
    }

    /**
     * @target expand()
     * @scenario Append large content to force segment expansion
     * @defectRisk Verify multi-segment handling
     */
    @Test(timeout = 4000)
    public void testExpandSegments() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            sb.append('x');
        }
        textBuffer.append(sb.toString(), 0, sb.length());
        assertEquals(sb.length(), textBuffer.size());
        assertEquals(sb.toString(), textBuffer.contentsAsString());
    }

    /**
     * @target contentsAsArray()
     * @scenario Get contents as character array
     * @defectRisk Verify array conversion
     */
    @Test(timeout = 4000)
    public void testContentsAsArray() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.append("array", 0, 5);
        char[] result = textBuffer.contentsAsArray();
        assertEquals("array", new String(result));
    }

    /**
     * @target contentsAsDouble()
     * @scenario Parse numeric content as double
     * @defectRisk Verify double parsing
     */
    @Test(timeout = 4000)
    public void testContentsAsDouble() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.append("3.14", 0, 4);
        assertEquals(3.14, textBuffer.contentsAsDouble(), 0.0001);
    }

    /**
     * @target contentsAsDecimal()
     * @scenario Parse numeric content as BigDecimal
     * @defectRisk Verify BigDecimal parsing
     */
    @Test(timeout = 4000)
    public void testContentsAsDecimalValid() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.append("123.45", 0, 6);
        BigDecimal result = textBuffer.contentsAsDecimal();
        assertEquals(new BigDecimal("123.45"), result);
    }

    /**
     * @target releaseBuffers()
     * @scenario Release buffers and verify reset
     * @defectRisk Verify buffer recycling
     */
    @Test(timeout = 4000)
    public void testReleaseBuffers() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.append("test", 0, 4);
        textBuffer.releaseBuffers();
        assertEquals(0, textBuffer.size());
        assertEquals("", textBuffer.contentsAsString());
    }

    /**
     * @target getCurrentSegment()
     * @scenario Get current segment and modify
     * @defectRisk Verify segment access
     */
    @Test(timeout = 4000)
    public void testGetCurrentSegment() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        char[] segment = textBuffer.getCurrentSegment();
        assertNotNull(segment);
        assertTrue(segment.length > 0);
    }

    /**
     * @target emptyAndGetCurrentSegment()
     * @scenario Empty buffer and get current segment
     * @defectRisk Verify segment reset
     */
    @Test(timeout = 4000)
    public void testEmptyAndGetCurrentSegment() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.append("test", 0, 4);
        char[] segment = textBuffer.emptyAndGetCurrentSegment();
        assertNotNull(segment);
        assertEquals(0, textBuffer.size());
    }

    /**
     * @target getCurrentSegmentSize()
     * @scenario Get current segment size
     * @defectRisk Verify size tracking
     */
    @Test(timeout = 4000)
    public void testGetCurrentSegmentSize() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.append("test", 0, 4);
        assertEquals(4, textBuffer.getCurrentSegmentSize());
    }

    /**
     * @target setCurrentLength()
     * @scenario Set current segment length
     * @defectRisk Verify length modification
     */
    @Test(timeout = 4000)
    public void testSetCurrentLength() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.append("test", 0, 4);
        textBuffer.setCurrentLength(2);
        assertEquals(2, textBuffer.size());
        assertEquals("te", textBuffer.contentsAsString());
    }

    /**
     * @target finishCurrentSegment()
     * @scenario Finish current segment and start new one
     * @defectRisk Verify segment completion
     */
    @Test(timeout = 4000)
    public void testFinishCurrentSegment() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.append("test", 0, 4);
        char[] newSegment = textBuffer.finishCurrentSegment();
        assertNotNull(newSegment);
        assertEquals(4, textBuffer.size());
    }

    /**
     * @target expandCurrentSegment()
     * @scenario Expand current segment
     * @defectRisk Verify segment expansion
     */
    @Test(timeout = 4000)
    public void testExpandCurrentSegment() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        char[] segment = textBuffer.getCurrentSegment();
        int oldLength = segment.length;
        char[] expanded = textBuffer.expandCurrentSegment();
        assertTrue(expanded.length > oldLength);
    }

    /**
     * @target unshare()
     * @scenario Unshare shared buffer
     * @defectRisk Verify unsharing behavior
     */
    @Test(timeout = 4000)
    public void testUnshare() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        char[] shared = "shared".toCharArray();
        textBuffer.resetWithShared(shared, 0, 6);
        textBuffer.append('!');
        assertEquals(7, textBuffer.size());
        assertEquals("shared!", textBuffer.contentsAsString());
    }

    /**
     * @target toString()
     * @scenario Convert to string
     * @defectRisk Verify string conversion
     */
    @Test(timeout = 4000)
    public void testToString() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.append("test", 0, 4);
        assertEquals("test", textBuffer.toString());
    }

    /**
     * @target getTextOffset()
     * @scenario Get text offset for shared buffer
     * @defectRisk Verify offset handling
     */
    @Test(timeout = 4000)
    public void testGetTextOffset() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        char[] shared = "shared".toCharArray();
        textBuffer.resetWithShared(shared, 2, 4);
        assertEquals(2, textBuffer.getTextOffset());
    }

    /**
     * @target hasTextAsCharacters()
     * @scenario Check character availability
     * @defectRisk Verify character access
     */
    @Test(timeout = 4000)
    public void testHasTextAsCharacters() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.append("test", 0, 4);
        assertTrue(textBuffer.hasTextAsCharacters());
    }

    /**
     * @target getTextBuffer()
     * @scenario Get text buffer
     * @defectRisk Verify buffer access
     */
    @Test(timeout = 4000)
    public void testGetTextBuffer() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.append("test", 0, 4);
        char[] buffer = textBuffer.getTextBuffer();
        assertNotNull(buffer);
        assertEquals("test", new String(buffer, 0, textBuffer.size()));
    }

    /**
     * @target buildResultArray()
     * @scenario Build result array from segments
     * @defectRisk Verify array construction
     */
    @Test(timeout = 4000)
    public void testBuildResultArray() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        for (int i = 0; i < 2000; i++) {
            textBuffer.append('a');
        }
        char[] result = textBuffer.contentsAsArray();
        assertEquals(2000, result.length);
    }

    /**
     * @target contentsAsString()
     * @scenario Get contents as string with multiple segments
     * @defectRisk Verify string construction
     */
    @Test(timeout = 4000)
    public void testContentsAsStringMultipleSegments() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3000; i++) {
            sb.append('b');
        }
        textBuffer.append(sb.toString(), 0, sb.length());
        assertEquals(sb.toString(), textBuffer.contentsAsString());
    }

    /**
     * @target append() with null allocator
     * @scenario Use TextBuffer with null BufferRecycler
     * @defectRisk Verify null allocator handling
     */
    @Test(timeout = 4000)
    public void testNullAllocator() {
        TextBuffer textBuffer = new TextBuffer(null);
        textBuffer.append("test", 0, 4);
        assertEquals(4, textBuffer.size());
        assertEquals("test", textBuffer.contentsAsString());
    }

    /**
     * @target resetWithString()
     * @scenario Reset with string value
     * @defectRisk Verify string reset
     */
    @Test(timeout = 4000)
    public void testResetWithString() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        textBuffer.resetWithString("test");
        assertEquals(4, textBuffer.size());
        assertEquals("test", textBuffer.contentsAsString());
    }

    /**
     * @target ensureNotShared()
     * @scenario Ensure buffer is not shared
     * @defectRisk Verify unsharing
     */
    @Test(timeout = 4000)
    public void testEnsureNotShared() {
        TextBuffer textBuffer = new TextBuffer(new BufferRecycler());
        char[] shared = "shared".toCharArray();
        textBuffer.resetWithShared(shared, 0, 6);
        textBuffer.ensureNotShared();
        textBuffer.append('!');
        assertEquals(7, textBuffer.size());
        assertEquals("shared!", textBuffer.contentsAsString());
    }
}