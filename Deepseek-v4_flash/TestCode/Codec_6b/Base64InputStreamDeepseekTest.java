package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: Base64InputStream
 * 
 * Decision Branches Covered:
 * 1. Constructor selection (no-arg vs encode flag vs full params)
 * 2. read() method: r == 0 loop, r > 0 branch, singleByte[0] < 0 branch, return -1
 * 3. read(byte[], int, int) method:
 *    - b == null -> NullPointerException
 *    - offset < 0 || len < 0 -> IndexOutOfBoundsException
 *    - offset > b.length || offset + len > b.length -> IndexOutOfBoundsException
 *    - len == 0 -> return 0
 *    - !base64.hasData() branch (inner: doEncode ? 4096 : 8192 buffer, c > 0 && b.length == len optimization)
 *    - doEncode true/false paths for encode/decode
 *    - base64.readResults() return value (0, >0, -1)
 * 4. markSupported() -> false
 * 
 * Boundary Conditions:
 * - Empty input stream
 * - Single byte reads
 * - Multiple byte reads with various offsets/lengths
 * - Non-base64 data (triggering readResults() returning 0)
 * - Large data streams
 * - Encode vs decode modes
 * - Line length and separator variations
 * 
 * Defect Target (CODEC-101):
 * - When readResults() returns 0 due to non-base64 data, the while-loop in read()
 *   must handle this correctly. The defect causes read() to return 0 instead of
 *   continuing to read until data is available or EOF.
 */
public class Base64InputStreamDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testDecodeSimpleString() throws IOException {
        byte[] encoded = "SGVsbG8gV29ybGQ=".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(encoded);
        Base64InputStream b64in = new Base64InputStream(in);
        byte[] result = new byte[100];
        int len = b64in.read(result);
        String decoded = new String(result, 0, len, "UTF-8");
        assertEquals("Hello World", decoded);
        b64in.close();
    }

    @Test(timeout = 4000)
    public void testEncodeSimpleString() throws IOException {
        byte[] plain = "Hello World".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(plain);
        Base64InputStream b64in = new Base64InputStream(in, true);
        byte[] result = new byte[100];
        int len = b64in.read(result);
        String encoded = new String(result, 0, len, "UTF-8");
        assertEquals("SGVsbG8gV29ybGQ=", encoded);
        b64in.close();
    }

    @Test(timeout = 4000)
    public void testDecodeWithLineLength() throws IOException {
        byte[] encoded = "SGVsbG8g\r\nV29ybGQ=".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(encoded);
        Base64InputStream b64in = new Base64InputStream(in, false, 10, new byte[]{'\r', '\n'});
        byte[] result = new byte[100];
        int len = b64in.read(result);
        String decoded = new String(result, 0, len, "UTF-8");
        assertEquals("Hello World", decoded);
        b64in.close();
    }

    @Test(timeout = 4000)
    public void testEncodeWithLineLength() throws IOException {
        byte[] plain = "Hello World".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(plain);
        Base64InputStream b64in = new Base64InputStream(in, true, 4, new byte[]{'\n'});
        byte[] result = new byte[100];
        int len = b64in.read(result);
        String encoded = new String(result, 0, len, "UTF-8");
        assertTrue(encoded.contains("\n"));
        b64in.close();
    }

    @Test(timeout = 4000)
    public void testReadSingleByteDecode() throws IOException {
        byte[] encoded = "QQ==".getBytes("UTF-8"); // encodes 'A'
        InputStream in = new ByteArrayInputStream(encoded);
        Base64InputStream b64in = new Base64InputStream(in);
        int b = b64in.read();
        assertEquals('A', b);
        assertEquals(-1, b64in.read());
        b64in.close();
    }

    @Test(timeout = 4000)
    public void testReadSingleByteEncode() throws IOException {
        byte[] plain = "A".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(plain);
        Base64InputStream b64in = new Base64InputStream(in, true);
        int b = b64in.read();
        assertEquals('Q', b); // 'A' encodes to 'QQ==', first byte is 'Q'
        b64in.close();
    }

    @Test(timeout = 4000)
    public void testReadSingleByteNegativeValue() throws IOException {
        byte[] encoded = {(byte)0x80, (byte)0x80, (byte)0x80, (byte)0x80}; // invalid base64
        InputStream in = new ByteArrayInputStream(encoded);
        Base64InputStream b64in = new Base64InputStream(in);
        // Should handle gracefully, may return -1 or some value
        int b = b64in.read();
        assertTrue(b == -1 || (b >= 0 && b <= 255));
        b64in.close();
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testEmptyInputStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Base64InputStream b64in = new Base64InputStream(in);
        assertEquals(-1, b64in.read());
        byte[] buf = new byte[10];
        assertEquals(-1, b64in.read(buf, 0, 10));
        b64in.close();
    }

    @Test(timeout = 4000)
    public void testReadWithZeroLength() throws IOException {
        byte[] data = "QQ==".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(data);
        Base64InputStream b64in = new Base64InputStream(in);
        byte[] buf = new byte[10];
        assertEquals(0, b64in.read(buf, 0, 0));
        b64in.close();
    }

    @Test(timeout = 4000)
    public void testReadWithLargeBuffer() throws IOException {
        // Generate a large base64 encoded string
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("QQ==");
        }
        byte[] encoded = sb.toString().getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(encoded);
        Base64InputStream b64in = new Base64InputStream(in);
        byte[] buf = new byte[2000];
        int total = 0;
        int r;
        while ((r = b64in.read(buf, total, buf.length - total)) > 0) {
            total += r;
        }
        assertEquals(1000, total); // 1000 'A' characters decoded
        b64in.close();
    }

    @Test(timeout = 4000)
    public void testReadWithOffsetAndLength() throws IOException {
        byte[] encoded = "SGVsbG8gV29ybGQ=".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(encoded);
        Base64InputStream b64in = new Base64InputStream(in);
        byte[] buf = new byte[20];
        int len = b64in.read(buf, 5, 10);
        assertEquals("Hello", new String(buf, 5, len, "UTF-8"));
        b64in.close();
    }

    @Test(timeout = 4000)
    public void testMarkSupported() {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Base64InputStream b64in = new Base64InputStream(in);
        assertFalse(b64in.markSupported());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone (CODEC-101) ====================

    /**
     * Directly targets the CODEC-101 defect.
     * When readResults() returns 0 due to non-base64 data in the input,
     * the read() method must loop until it gets data or EOF.
     * The defect causes read() to return 0 instead of continuing.
     */
    @Test(timeout = 4000)
    public void testCodec101Defect() throws IOException {
        // Create input with non-base64 data interspersed
        // The base64 decoder will skip non-base64 characters and may return 0
        byte[] input = "!!SGVsbG8g!!V29ybGQ=".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(input);
        Base64InputStream b64in = new Base64InputStream(in);
        
        byte[] buf = new byte[100];
        int totalRead = 0;
        int r;
        while ((r = b64in.read(buf, totalRead, buf.length - totalRead)) > 0) {
            totalRead += r;
        }
        
        // Should successfully decode "Hello World" despite non-base64 characters
        String decoded = new String(buf, 0, totalRead, "UTF-8");
        assertEquals("Hello World", decoded);
        b64in.close();
    }

    @Test(timeout = 4000)
    public void testCodec101DefectSingleByte() throws IOException {
        // Test the single-byte read path with non-base64 data
        byte[] input = "!!QQ==".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(input);
        Base64InputStream b64in = new Base64InputStream(in);
        
        // Should read 'A' despite leading non-base64 characters
        int b = b64in.read();
        assertEquals('A', b);
        assertEquals(-1, b64in.read());
        b64in.close();
    }

    @Test(timeout = 4000)
    public void testCodec101DefectAllNonBase64() throws IOException {
        // Input with only non-base64 characters
        byte[] input = "!!!!!".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(input);
        Base64InputStream b64in = new Base64InputStream(in);
        
        // Should return -1 (EOF) since no valid base64 data
        assertEquals(-1, b64in.read());
        b64in.close();
    }

    @Test(timeout = 4000)
    public void testCodec101DefectMixedReads() throws IOException {
        // Test alternating single-byte and multi-byte reads with non-base64 data
        byte[] input = "!!SGVsbG8g!!V29ybGQ=".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(input);
        Base64InputStream b64in = new Base64InputStream(in);
        
        // Read first byte
        int b1 = b64in.read();
        assertEquals('H', b1);
        
        // Read remaining with buffer
        byte[] buf = new byte[100];
        int len = b64in.read(buf);
        String remaining = new String(buf, 0, len, "UTF-8");
        assertEquals("ello World", remaining);
        b64in.close();
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testReadNullBuffer() throws IOException {
        InputStream in = new ByteArrayInputStream("QQ==".getBytes("UTF-8"));
        Base64InputStream b64in = new Base64InputStream(in);
        b64in.read(null, 0, 10);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReadNegativeOffset() throws IOException {
        InputStream in = new ByteArrayInputStream("QQ==".getBytes("UTF-8"));
        Base64InputStream b64in = new Base64InputStream(in);
        b64in.read(new byte[10], -1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReadNegativeLength() throws IOException {
        InputStream in = new ByteArrayInputStream("QQ==".getBytes("UTF-8"));
        Base64InputStream b64in = new Base64InputStream(in);
        b64in.read(new byte[10], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReadOffsetExceedsBuffer() throws IOException {
        InputStream in = new ByteArrayInputStream("QQ==".getBytes("UTF-8"));
        Base64InputStream b64in = new Base64InputStream(in);
        b64in.read(new byte[10], 15, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReadOffsetPlusLengthExceedsBuffer() throws IOException {
        InputStream in = new ByteArrayInputStream("QQ==".getBytes("UTF-8"));
        Base64InputStream b64in = new Base64InputStream(in);
        b64in.read(new byte[10], 5, 10);
    }

    @Test(timeout = 4000)
    public void testReadWithClosedStream() throws IOException {
        InputStream in = new ByteArrayInputStream("QQ==".getBytes("UTF-8"));
        Base64InputStream b64in = new Base64InputStream(in);
        b64in.close();
        // Behavior after close is undefined but should not throw NPE
        try {
            b64in.read();
        } catch (IOException e) {
            // Expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testMultipleReadsSameStream() throws IOException {
        byte[] encoded = "SGVsbG8gV29ybGQ=".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(encoded);
        Base64InputStream b64in = new Base64InputStream(in);
        
        byte[] buf1 = new byte[5];
        int len1 = b64in.read(buf1);
        assertEquals("Hello", new String(buf1, 0, len1, "UTF-8"));
        
        byte[] buf2 = new byte[10];
        int len2 = b64in.read(buf2);
        assertEquals(" World", new String(buf2, 0, len2, "UTF-8"));
        
        assertEquals(-1, b64in.read());
        b64in.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithLineSeparator() throws IOException {
        byte[] encoded = "SGVsbG8gV29ybGQ=".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(encoded);
        Base64InputStream b64in = new Base64InputStream(in, false, 0, new byte[]{});
        byte[] result = new byte[100];
        int len = b64in.read(result);
        assertEquals("Hello World", new String(result, 0, len, "UTF-8"));
        b64in.close();
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeRoundTrip() throws IOException {
        String original = "Test data with special chars: !@#$%^&*()";
        byte[] plain = original.getBytes("UTF-8");
        
        // Encode
        InputStream in1 = new ByteArrayInputStream(plain);
        Base64InputStream encoder = new Base64InputStream(in1, true);
        byte[] encoded = new byte[200];
        int encLen = encoder.read(encoded);
        encoder.close();
        
        // Decode
        InputStream in2 = new ByteArrayInputStream(encoded, 0, encLen);
        Base64InputStream decoder = new Base64InputStream(in2, false);
        byte[] decoded = new byte[200];
        int decLen = decoder.read(decoded);
        decoder.close();
        
        assertEquals(original, new String(decoded, 0, decLen, "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testLargeDataStream() throws IOException {
        // Create a large base64 encoded stream
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("QQ==");
        }
        byte[] encoded = sb.toString().getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(encoded);
        Base64InputStream b64in = new Base64InputStream(in);
        
        byte[] buf = new byte[8192];
        int total = 0;
        int r;
        while ((r = b64in.read(buf)) > 0) {
            total += r;
        }
        assertEquals(10000, total); // 10000 'A' characters
        b64in.close();
    }
}