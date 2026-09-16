/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.codec.binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.codec.binary.Base64InputStream
 * 
 * Branches & Conditions Targeted:
 * 1. Constructor Variations:
 *    - Base64InputStream(in): Defaults doEncode=false, lineLength=0, lineSeparator=CHUNK_SEPARATOR.
 *    - Base64InputStream(in, doEncode): Custom encode/decode flag.
 *    - Base64InputStream(in, doEncode, lineLength, lineSeparator): Custom line chunking & delimiters.
 * 2. read():
 *    - while (r == 0): Loop when intermediate read returns 0.
 *    - if (r > 0):
 *        - singleByte[0] < 0: Signed byte conversion (256 + singleByte[0]) for values 128..255.
 *        - singleByte[0] >= 0: Unsigned byte direct return for values 0..127.
 *    - r <= 0 (EOF): Returns -1.
 * 3. read(byte[] b, int offset, int len):
 *    - b == null: Throws NullPointerException.
 *    - offset < 0 || len < 0: Throws IndexOutOfBoundsException.
 *    - offset > b.length || offset + len > b.length: Throws IndexOutOfBoundsException.
 *    - len == 0: Returns 0 without processing.
 *    - while (readLen == 0):
 *        - !base64.hasData():
 *            - doEncode == true: Allocates 4096 byte read buffer.
 *            - doEncode == false: Allocates 8192 byte read buffer.
 *            - c > 0 && b.length == len: Calls base64.setInitialBuffer(b, offset, len).
 *            - c > 0 && b.length != len: Skips setInitialBuffer.
 *            - c <= 0: EOF reached on underlying stream.
 *            - doEncode == true: Invokes base64.encode(buf, 0, c).
 *            - doEncode == false: Invokes base64.decode(buf, 0, c).
 * 4. markSupported(): Always returns false.
 * 
 * Defects4J Ground Truth Defect:
 * - Bug: CODEC-105 (ArrayIndexOutOfBoundsException: 2)
 * - Cause: When encoding data and read() is invoked, read(singleByte, 0, 1) is called where
 *          singleByte.length == len (1 == 1). This triggers base64.setInitialBuffer(singleByte, 0, 1).
 *          On EOF flush, Base64 attempts to flush 4 encoded bytes into a buffer that was initialized
 *          to size 1 and doubles to 2 via resizeBuffer(), throwing ArrayIndexOutOfBoundsException: 2.
 * - Test Target: testCodec105(), testCodec105WithDefaults(), testCodec105SmallArrayRead().
 * ---------------------------------------------------------------------------------------------------------
 */
public class Base64InputStreamGeminiTest {

    private static final byte[] CRLF = new byte[] { '\r', '\n' };

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEncodeDecodeRoundTripByteByByte() throws IOException {
        byte[] original = new byte[256];
        for (int i = 0; i < 256; i++) {
            original[i] = (byte) i;
        }

        // Encode
        ByteArrayInputStream bais = new ByteArrayInputStream(original);
        Base64InputStream encoder = new Base64InputStream(bais, true);
        ByteArrayOutputStream encodedOut = new ByteArrayOutputStream();
        int b;
        while ((b = encoder.read()) != -1) {
            encodedOut.write(b);
        }
        encoder.close();

        // Decode
        ByteArrayInputStream encodedIn = new ByteArrayInputStream(encodedOut.toByteArray());
        Base64InputStream decoder = new Base64InputStream(encodedIn, false);
        ByteArrayOutputStream decodedOut = new ByteArrayOutputStream();
        while ((b = decoder.read()) != -1) {
            decodedOut.write(b);
        }
        decoder.close();

        assertArrayEquals(original, decodedOut.toByteArray());
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeChunkedWithCustomSeparator() throws IOException {
        String testString = "Hello Defects4J World! Testing RFC 2045 Line Chunking Behavior.";
        byte[] inputBytes = testString.getBytes("UTF-8");

        byte[] customSeparator = new byte[] { '$', '#' };
        int lineLength = 16;

        Base64InputStream encoder = new Base64InputStream(
                new ByteArrayInputStream(inputBytes), true, lineLength, customSeparator);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8];
        int read;
        while ((read = encoder.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, read);
        }
        encoder.close();

        byte[] encodedData = baos.toByteArray();
        assertTrue(encodedData.length > 0);

        // Decode using the default decoder constructor
        Base64InputStream decoder = new Base64InputStream(new ByteArrayInputStream(encodedData));
        ByteArrayOutputStream decodedOut = new ByteArrayOutputStream();
        while ((read = decoder.read(buffer, 0, buffer.length)) != -1) {
            decodedOut.write(buffer, 0, read);
        }
        decoder.close();

        assertArrayEquals(inputBytes, decodedOut.toByteArray());
    }

    @Test(timeout = 4000)
    public void testReadUnsignedByteConversion() throws IOException {
        // Base64 "////" decodes to 3 bytes of 0xFF (-1 in signed byte, 255 in unsigned byte)
        byte[] allOnesBase64 = "////".getBytes("US-ASCII");
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(allOnesBase64));

        assertEquals(255, in.read());
        assertEquals(255, in.read());
        assertEquals(255, in.read());
        assertEquals(-1, in.read());
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadZeroAndPositiveBytes() throws IOException {
        // Base64 "AAAA" decodes to 3 zero bytes (0, 0, 0)
        byte[] zerosBase64 = "AAAA".getBytes("US-ASCII");
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(zerosBase64));

        assertEquals(0, in.read());
        assertEquals(0, in.read());
        assertEquals(0, in.read());
        assertEquals(-1, in.read());
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadWithBufferOffset() throws IOException {
        byte[] encoded = "SGVsbG8=".getBytes("US-ASCII"); // "Hello"
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(encoded));

        byte[] dest = new byte[10];
        Arrays.fill(dest, (byte) 0xFF);

        int bytesRead = in.read(dest, 2, 5);
        assertEquals(5, bytesRead);
        assertEquals((byte) 0xFF, dest[0]);
        assertEquals((byte) 0xFF, dest[1]);
        assertEquals((byte) 'H', dest[2]);
        assertEquals((byte) 'e', dest[3]);
        assertEquals((byte) 'l', dest[4]);
        assertEquals((byte) 'l', dest[5]);
        assertEquals((byte) 'o', dest[6]);
        assertEquals((byte) 0xFF, dest[7]);
        in.close();
    }

    @Test(timeout = 4000)
    public void testLargeStreamEncodingAndDecoding() throws IOException {
        // Test large payload exceeding 4096 (encode buffer) and 8192 (decode buffer)
        byte[] largeData = new byte[16384];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) ((i * 31) & 0xFF);
        }

        Base64InputStream encoder = new Base64InputStream(new ByteArrayInputStream(largeData), true);
        ByteArrayOutputStream encodedStream = new ByteArrayOutputStream();
        byte[] transferBuf = new byte[1024];
        int count;
        while ((count = encoder.read(transferBuf)) != -1) {
            encodedStream.write(transferBuf, 0, count);
        }
        encoder.close();

        Base64InputStream decoder = new Base64InputStream(new ByteArrayInputStream(encodedStream.toByteArray()), false);
        ByteArrayOutputStream decodedStream = new ByteArrayOutputStream();
        while ((count = decoder.read(transferBuf)) != -1) {
            decodedStream.write(transferBuf, 0, count);
        }
        decoder.close();

        assertArrayEquals(largeData, decodedStream.toByteArray());
    }

    @Test(timeout = 4000)
    public void testCodec101WhileLoopOnReadResultsZero() throws IOException {
        // Stream with many non-base64 whitespace characters causing readResults to return 0 initially
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9000; i++) {
            sb.append(' ');
        }
        sb.append("AAAA"); // decodes to 0, 0, 0
        byte[] input = sb.toString().getBytes("US-ASCII");

        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(input));
        byte[] result = new byte[3];
        int read = in.read(result, 0, 3);
        assertEquals(3, read);
        assertArrayEquals(new byte[] { 0, 0, 0 }, result);
        in.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyInputStreamEncoding() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]), true);
        assertEquals(-1, in.read());
        in.close();
    }

    @Test(timeout = 4000)
    public void testEmptyInputStreamDecoding() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]), false);
        assertEquals(-1, in.read());
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadZeroLengthArray() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));
        assertEquals(0, in.read(new byte[10], 0, 0));
        assertEquals(0, in.read(new byte[0], 0, 0));
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadPastEndOfStreamRepeatedly() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        assertEquals(-1, in.read());
        assertEquals(-1, in.read());
        assertEquals(-1, in.read(new byte[10], 0, 10));
        assertEquals(-1, in.read(new byte[10], 0, 10));
        in.close();
    }

    @Test(timeout = 4000)
    public void testEncodingWithNegativeOrZeroLineLength() throws IOException {
        byte[] data = "SampleDataForNoLineSeparator".getBytes("UTF-8");

        Base64InputStream inZero = new Base64InputStream(new ByteArrayInputStream(data), true, 0, CRLF);
        ByteArrayOutputStream baosZero = new ByteArrayOutputStream();
        int b;
        while ((b = inZero.read()) != -1) {
            baosZero.write(b);
        }
        inZero.close();

        Base64InputStream inNegative = new Base64InputStream(new ByteArrayInputStream(data), true, -1, CRLF);
        ByteArrayOutputStream baosNegative = new ByteArrayOutputStream();
        while ((b = inNegative.read()) != -1) {
            baosNegative.write(b);
        }
        inNegative.close();

        assertArrayEquals(baosZero.toByteArray(), baosNegative.toByteArray());
        assertFalse(new String(baosZero.toByteArray(), "US-ASCII").contains("\r\n"));
    }

    @Test(timeout = 4000)
    public void testReadBufferWhereLengthNotEqualToLen() throws IOException {
        // Forces c > 0 && b.length == len to evaluate FALSE
        byte[] input = "SGVsbG8=".getBytes("US-ASCII");
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(input));
        byte[] dest = new byte[100];
        int read = in.read(dest, 0, 10);
        assertEquals(5, read);
        in.close();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CODEC-105)
    // =========================================================================

    /**
     * Target Defect: CODEC-105
     * Ground Truth Failure: java.lang.ArrayIndexOutOfBoundsException: 2
     * Trigger: Encoding 1 byte via read() or read(buf, 0, 1) where buf.length == len.
     */
    @Test(timeout = 4000)
    public void testCodec105() throws IOException {
        final byte[] input = new byte[] { (byte) 0 };
        final InputStream bais = new ByteArrayInputStream(input);
        final Base64InputStream in = new Base64InputStream(bais, true, 0, null);

        // Encoding byte 0 in Base64 produces 4 bytes: "AA==" -> 'A', 'A', '=', '='
        assertEquals('A', in.read());
        assertEquals('A', in.read());
        assertEquals('=', in.read());
        assertEquals('=', in.read());
        assertEquals(-1, in.read());
        in.close();
    }

    @Test(timeout = 4000)
    public void testCodec105WithDefaults() throws IOException {
        final byte[] input = new byte[] { (byte) 'f' }; // 'f' encoded is "Zg=="
        final InputStream bais = new ByteArrayInputStream(input);
        final Base64InputStream in = new Base64InputStream(bais, true);

        assertEquals('Z', in.read());
        assertEquals('g', in.read());
        assertEquals('=', in.read());
        assertEquals('=', in.read());
        assertEquals(-1, in.read());
        in.close();
    }

    @Test(timeout = 4000)
    public void testCodec105SmallArrayRead() throws IOException {
        final byte[] input = new byte[] { (byte) 0 };
        final InputStream bais = new ByteArrayInputStream(input);
        final Base64InputStream in = new Base64InputStream(bais, true, 0, null);

        byte[] dest = new byte[1];
        int r1 = in.read(dest, 0, 1);
        assertEquals(1, r1);
        assertEquals((byte) 'A', dest[0]);

        int r2 = in.read(dest, 0, 1);
        assertEquals(1, r2);
        assertEquals((byte) 'A', dest[0]);

        int r3 = in.read(dest, 0, 1);
        assertEquals(1, r3);
        assertEquals((byte) '=', dest[0]);

        int r4 = in.read(dest, 0, 1);
        assertEquals(1, r4);
        assertEquals((byte) '=', dest[0]);

        int r5 = in.read(dest, 0, 1);
        assertEquals(-1, r5);
        in.close();
    }

    @Test(timeout = 4000)
    public void testCodec105ArrayLengthTwo() throws IOException {
        final byte[] input = new byte[] { (byte) 0 };
        final InputStream bais = new ByteArrayInputStream(input);
        final Base64InputStream in = new Base64InputStream(bais, true, 0, null);

        byte[] dest = new byte[2];
        int r1 = in.read(dest, 0, 2);
        assertEquals(2, r1);
        assertEquals((byte) 'A', dest[0]);
        assertEquals((byte) 'A', dest[1]);

        int r2 = in.read(dest, 0, 2);
        assertEquals(2, r2);
        assertEquals((byte) '=', dest[0]);
        assertEquals((byte) '=', dest[1]);

        int r3 = in.read(dest, 0, 2);
        assertEquals(-1, r3);
        in.close();
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testReadNullByteArrayThrowsNPE() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[10]));
        try {
            in.read(null, 0, 1);
        } finally {
            in.close();
        }
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadNegativeOffsetThrowsIOOBE() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[10]));
        try {
            in.read(new byte[10], -1, 1);
        } finally {
            in.close();
        }
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadNegativeLenThrowsIOOBE() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[10]));
        try {
            in.read(new byte[10], 0, -1);
        } finally {
            in.close();
        }
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadOffsetGreaterThanLengthThrowsIOOBE() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[10]));
        try {
            in.read(new byte[10], 11, 0);
        } finally {
            in.close();
        }
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadOffsetPlusLenGreaterThanLengthThrowsIOOBE() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[10]));
        try {
            in.read(new byte[10], 5, 6);
        } finally {
            in.close();
        }
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testUnderlyingStreamThrowsIOException() throws IOException {
        InputStream faultyStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Underlying stream failure");
            }
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("Underlying stream failure");
            }
        };
        Base64InputStream in = new Base64InputStream(faultyStream);
        try {
            in.read();
        } finally {
            in.close();
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testMarkSupportedAlwaysFalse() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[10]));
        assertFalse(in.markSupported());
        in.close();
    }

    @Test(timeout = 4000)
    public void testCloseDelegatesToUnderlyingStream() throws IOException {
        final boolean[] closed = new boolean[1];
        InputStream is = new InputStream() {
            @Override
            public int read() {
                return -1;
            }
            @Override
            public void close() {
                closed[0] = true;
            }
        };

        Base64InputStream in = new Base64InputStream(is);
        assertFalse(closed[0]);
        in.close();
        assertTrue(closed[0]);
    }
}