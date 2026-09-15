package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base64InputStreamDeepseekTest
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: org.apache.commons.codec.binary.Base64InputStream
 * 
 * Decision Branches Targeted:
 * B1: read(byte[], int, int) - b == null -> NullPointerException
 * B2: read(byte[], int, int) - offset < 0 || len < 0 -> IndexOutOfBoundsException
 * B3: read(byte[], int, int) - offset > b.length || offset + len > b.length -> IndexOutOfBoundsException
 * B4: read(byte[], int, int) - len == 0 -> return 0
 * B5: read(byte[], int, int) - while (readLen == 0) loop iteration
 * B6: read(byte[], int, int) - !base64.hasData() branch (true/false)
 * B7: read(byte[], int, int) - c > 0 && b.length == len (setInitialBuffer call)
 * B8: read(byte[], int, int) - doEncode ? encode : decode branch
 * B9: read() - r > 0 vs r == 0 vs r < 0 (return singleByte or -1)
 * B10: singleByte[0] < 0 ? 256 + singleByte[0] : singleByte[0] (signed byte handling)
 * 
 * Boundary Conditions:
 * BC1: Empty input stream (len 0)
 * BC2: Single byte input
 * BC3: Multiple bytes exactly filling buffer
 * BC4: Multiple bytes exceeding buffer
 * BC5: Invalid offset/len combinations
 * BC6: Defect target: CODEC-105 / testCodec105 pattern triggering ArrayIndexOutOfBoundsException
 * 
 * Defect Specification:
 * - Known bug causing ArrayIndexOutOfBoundsException when specific input patterns are decoded
 * - The defect manifests when base64 decoding encounters specific state conditions
 * - Test must expose this by reading encoded data in specific patterns
 */
public class Base64InputStreamDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testDecodeSimpleString() throws IOException {
        String encoded = "SGVsbG8gV29ybGQ="; // "Hello World" in Base64
        byte[] encodedBytes = encoded.getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(encodedBytes);
        Base64InputStream bis = new Base64InputStream(in);
        
        byte[] output = new byte[100];
        int bytesRead = bis.read(output, 0, output.length);
        
        String decoded = new String(output, 0, bytesRead, "UTF-8");
        assertEquals("Hello World", decoded);
        assertEquals(11, bytesRead);
        bis.close();
    }

    @Test(timeout = 4000)
    public void testDecodeWithLineBreaks() throws IOException {
        // Base64 encoded with line breaks (76 chars per line)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("A"); // 'A' encodes to "QQ=="
        }
        String input = sb.toString();
        String encoded = java.util.Base64.getMimeEncoder().encodeToString(input.getBytes("UTF-8"));
        
        InputStream in = new ByteArrayInputStream(encoded.getBytes("UTF-8"));
        Base64InputStream bis = new Base64InputStream(in);
        
        byte[] output = new byte[200];
        int bytesRead = bis.read(output, 0, output.length);
        
        assertEquals(100, bytesRead);
        String decoded = new String(output, 0, bytesRead, "UTF-8");
        assertEquals(input, decoded);
        bis.close();
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeRoundTrip() throws IOException {
        byte[] original = "Test Data for Round Trip".getBytes("UTF-8");
        
        // Encode
        InputStream rawIn = new ByteArrayInputStream(original);
        Base64InputStream encoder = new Base64InputStream(rawIn, true);
        byte[] encodedData = new byte[100];
        int encodedLen = encoder.read(encodedData, 0, encodedData.length);
        encoder.close();
        
        // Decode (trim to actual encoded size)
        byte[] encodedTrimmed = new byte[encodedLen];
        System.arraycopy(encodedData, 0, encodedTrimmed, 0, encodedLen);
        
        InputStream encodedIn = new ByteArrayInputStream(encodedTrimmed);
        Base64InputStream decoder = new Base64InputStream(encodedIn, false);
        byte[] decodedData = new byte[100];
        int decodedLen = decoder.read(decodedData, 0, decodedData.length);
        decoder.close();
        
        byte[] decodedTrimmed = new byte[decodedLen];
        System.arraycopy(decodedData, 0, decodedTrimmed, 0, decodedLen);
        
        assertArrayEquals(original, decodedTrimmed);
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testEmptyInputStream() throws IOException {
        InputStream emptyIn = new ByteArrayInputStream(new byte[0]);
        Base64InputStream bis = new Base64InputStream(emptyIn);
        
        byte[] buffer = new byte[10];
        int bytesRead = bis.read(buffer, 0, buffer.length);
        
        assertEquals(-1, bytesRead);
        bis.close();
    }

    @Test(timeout = 4000)
    public void testSingleByteRead() throws IOException {
        String encoded = "QQ=="; // 'A' encoded
        InputStream in = new ByteArrayInputStream(encoded.getBytes("UTF-8"));
        Base64InputStream bis = new Base64InputStream(in);
        
        int firstByte = bis.read();
        assertEquals('A', firstByte);
        
        int secondByte = bis.read();
        assertEquals(-1, secondByte);
        
        bis.close();
    }

    @Test(timeout = 4000)
    public void testReadWithZeroLength() throws IOException {
        String encoded = "QQ==";
        InputStream in = new ByteArrayInputStream(encoded.getBytes("UTF-8"));
        Base64InputStream bis = new Base64InputStream(in);
        
        byte[] buffer = new byte[10];
        int bytesRead = bis.read(buffer, 0, 0);
        
        assertEquals(0, bytesRead);
        bis.close();
    }

    @Test(timeout = 4000)
    public void testExactBufferFill() throws IOException {
        // Create input that will produce exactly 10 bytes when decoded
        String input = "AAAAAAAAAA"; // 10 'A's
        String encoded = java.util.Base64.getEncoder().encodeToString(input.getBytes("UTF-8"));
        
        InputStream in = new ByteArrayInputStream(encoded.getBytes("UTF-8"));
        Base64InputStream bis = new Base64InputStream(in);
        
        byte[] buffer = new byte[10];
        int bytesRead = bis.read(buffer, 0, 10);
        
        assertEquals(10, bytesRead);
        for (int i = 0; i < 10; i++) {
            assertEquals('A', buffer[i]);
        }
        bis.close();
    }

    @Test(timeout = 4000)
    public void testReadToSmallBuffer() throws IOException {
        String input = "HelloWorld";
        String encoded = java.util.Base64.getEncoder().encodeToString(input.getBytes("UTF-8"));
        
        InputStream in = new ByteArrayInputStream(encoded.getBytes("UTF-8"));
        Base64InputStream bis = new Base64InputStream(in);
        
        byte[] buffer = new byte[5];
        int totalRead = 0;
        int bytesRead;
        StringBuilder result = new StringBuilder();
        
        while ((bytesRead = bis.read(buffer, 0, buffer.length)) != -1) {
            result.append(new String(buffer, 0, bytesRead, "UTF-8"));
            totalRead += bytesRead;
        }
        
        assertEquals(10, totalRead);
        assertEquals("HelloWorld", result.toString());
        bis.close();
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * This test targets the specific defect documented in CODEC-105.
     * The bug causes ArrayIndexOutOfBoundsException when base64 decoding
     * encounters a specific state during processing.
     * 
     * The test uses an input pattern that triggers the defect by:
     * 1. Providing encoded data that results in 0-length readResults() calls
     * 2. Then forcing a buffer reallocation that exposes the off-by-one error
     */
    @Test(timeout = 4000)
    public void testCodec105() throws IOException {
        // This specific input pattern triggers the ArrayIndexOutOfBoundsException
        // in the defective version of Base64InputStream
        byte[] input = new byte[] {
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
        };
        
        // Encode the input
        String encoded = java.util.Base64.getEncoder().encodeToString(input);
        
        // Use line separator to force specific buffer state
        Base64InputStream bis = new Base64InputStream(
            new ByteArrayInputStream(encoded.getBytes("UTF-8")),
            false,
            0,
            new byte[] {'\r', '\n'}
        );
        
        byte[] output = new byte[12];
        try {
            int bytesRead = bis.read(output, 0, output.length);
            assertTrue("Should read some bytes", bytesRead > 0);
            assertEquals(12, bytesRead);
            
            // Verify the decoded data matches original
            byte[] decodedTrimmed = new byte[bytesRead];
            System.arraycopy(output, 0, decodedTrimmed, 0, bytesRead);
            assertArrayEquals(input, decodedTrimmed);
        } catch (ArrayIndexOutOfBoundsException e) {
            fail("ArrayIndexOutOfBoundsException thrown - this is the CODEC-105 defect: " + e.getMessage());
        } finally {
            bis.close();
        }
    }

    @Test(timeout = 4000)
    public void testCodec105Variant() throws IOException {
        // Another variant that triggers the same defect
        // Using non-aligned input that causes readResults to return 0
        byte[] input = new byte[] {
            (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04,
            (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08
        };
        
        String encoded = java.util.Base64.getEncoder().encodeToString(input);
        
        // Use custom line separator to change internal buffer state
        Base64InputStream bis = new Base64InputStream(
            new ByteArrayInputStream(encoded.getBytes("UTF-8")),
            false,
            4,
            new byte[] {'\n'}
        );
        
        byte[] output = new byte[8];
        try {
            int bytesRead = bis.read(output, 0, output.length);
            assertEquals(8, bytesRead);
            
            // Verify content
            for (int i = 0; i < 8; i++) {
                assertEquals(input[i], output[i]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            fail("ArrayIndexOutOfBoundsException thrown - this is the CODEC-105 defect: " + e.getMessage());
        } finally {
            bis.close();
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullBuffer() throws IOException {
        String encoded = "QQ==";
        InputStream in = new ByteArrayInputStream(encoded.getBytes("UTF-8"));
        Base64InputStream bis = new Base64InputStream(in);
        
        try {
            bis.read(null, 0, 10);
        } finally {
            bis.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testNegativeOffset() throws IOException {
        String encoded = "QQ==";
        InputStream in = new ByteArrayInputStream(encoded.getBytes("UTF-8"));
        Base64InputStream bis = new Base64InputStream(in);
        
        try {
            bis.read(new byte[10], -1, 5);
        } finally {
            bis.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testNegativeLength() throws IOException {
        String encoded = "QQ==";
        InputStream in = new ByteArrayInputStream(encoded.getBytes("UTF-8"));
        Base64InputStream bis = new Base64InputStream(in);
        
        try {
            bis.read(new byte[10], 0, -1);
        } finally {
            bis.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testOffsetExceedsBufferLength() throws IOException {
        String encoded = "QQ==";
        InputStream in = new ByteArrayInputStream(encoded.getBytes("UTF-8"));
        Base64InputStream bis = new Base64InputStream(in);
        
        try {
            bis.read(new byte[10], 15, 5);
        } finally {
            bis.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testOffsetPlusLenExceedsBufferLength() throws IOException {
        String encoded = "QQ==";
        InputStream in = new ByteArrayInputStream(encoded.getBytes("UTF-8"));
        Base64InputStream bis = new Base64InputStream(in);
        
        try {
            bis.read(new byte[10], 5, 10);
        } finally {
            bis.close();
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testMarkNotSupported() {
        String encoded = "QQ==";
        InputStream in = new ByteArrayInputStream(encoded.getBytes("UTF-8"));
        Base64InputStream bis = new Base64InputStream(in);
        
        assertFalse("markSupported() should return false", bis.markSupported());
    }

    @Test(timeout = 4000)
    public void testMultipleReads() throws IOException {
        String input = "This is a longer test string that requires multiple reads to process completely";
        String encoded = java.util.Base64.getEncoder().encodeToString(input.getBytes("UTF-8"));
        
        InputStream in = new ByteArrayInputStream(encoded.getBytes("UTF-8"));
        Base64InputStream bis = new Base64InputStream(in);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[10];
        int bytesRead;
        
        while ((bytesRead = bis.read(buffer, 0, buffer.length)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        
        assertEquals(input, new String(output.toByteArray(), "UTF-8"));
        bis.close();
    }

    @Test(timeout = 4000)
    public void testConstructorDefaults() throws IOException {
        // Test that the single-argument constructor defaults to decode mode
        String encoded = "QQ==";
        InputStream in = new ByteArrayInputStream(encoded.getBytes("UTF-8"));
        Base64InputStream bis = new Base64InputStream(in);
        
        int b = bis.read();
        assertEquals('A', b);
        bis.close();
    }

    @Test(timeout = 4000)
    public void testCloseAfterRead() throws IOException {
        String encoded = "SGVsbG8=";
        InputStream in = new ByteArrayInputStream(encoded.getBytes("UTF-8"));
        Base64InputStream bis = new Base64InputStream(in);
        
        byte[] buffer = new byte[100];
        int bytesRead = bis.read(buffer, 0, buffer.length);
        assertTrue(bytesRead > 0);
        
        bis.close();
        
        // After close, read should return -1 (or throw, but we handle gracefully)
        try {
            int result = bis.read();
            assertTrue("After close, read() should return -1 or throw", result == -1);
        } catch (IOException e) {
            // IOException after close is acceptable behavior
        }
    }
}

// Helper class for collecting output in testMultipleReads
class ByteArrayOutputStream extends java.io.ByteArrayOutputStream {
    // Just using the standard ByteArrayOutputStream functionality
}