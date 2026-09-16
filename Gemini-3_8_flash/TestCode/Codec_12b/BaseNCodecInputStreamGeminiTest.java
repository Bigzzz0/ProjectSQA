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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.codec.binary.BaseNCodecInputStream
 *
 * Branch & Condition Coverage:
 * 1. read():
 *    - while (r == 0): loop iteration when read returns 0, terminates on r > 0 or EOF (-1)
 *    - if (r > 0):
 *        - byte b < 0: unsigned promotion (256 + b)
 *        - byte b >= 0: directly returned as int
 *    - returns EOF (-1) when stream exhausted
 * 2. read(byte[] b, int offset, int len):
 *    - b == null -> NullPointerException
 *    - offset < 0 -> IndexOutOfBoundsException
 *    - len < 0 -> IndexOutOfBoundsException
 *    - offset > b.length -> IndexOutOfBoundsException
 *    - offset + len > b.length -> IndexOutOfBoundsException
 *    - len == 0 -> returns 0 immediately without reading
 *    - while (readLen == 0):
 *        - !baseNCodec.hasData():
 *            - doEncode == true: allocates 4096-byte buffer and invokes encode()
 *            - doEncode == false: allocates 8192-byte buffer and invokes decode()
 *        - baseNCodec.hasData(): skips buffer fill and extracts via readResults()
 *    - readLen > 0 or readLen == EOF (-1)
 * 3. markSupported():
 *    - returns false unconditionally
 *
 * Defects4J Ground Truth Defect Zone (Codec-130 & missing overrides):
 * 1. BaseNCodecInputStream failing to properly implement skip() and available():
 *    - skip(-1) must throw IllegalArgumentException per specification.
 *    - skip(n) must skip decoded/encoded bytes without corrupting decoding state (Codec-130).
 *    - skip past stream end should return the actual bytes skipped.
 *    - available() must return 1 when data remains and 0 upon EOF.
 */
public class BaseNCodecInputStreamGeminiTest {

    private static final String HELLO_WORLD_STR = "Hello World";
    private static final byte[] HELLO_WORLD_BYTES = StringUtils.getBytesUtf8(HELLO_WORLD_STR);
    // Base64 representation of "Hello World"
    private static final byte[] HELLO_WORLD_B64 = StringUtils.getBytesUtf8("SGVsbG8gV29ybGQ=");

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDecodeSingleByteRead() throws IOException {
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);

        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = is.read()) != -1) {
            sb.append((char) b);
        }
        assertEquals(HELLO_WORLD_STR, sb.toString());
        assertEquals(-1, is.read());
        is.close();
    }

    @Test(timeout = 4000)
    public void testEncodeSingleByteRead() throws IOException {
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_BYTES);
        BaseNCodecInputStream is = new Base64InputStream(in, true);

        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = is.read()) != -1) {
            sb.append((char) b);
        }
        assertEquals("SGVsbG8gV29ybGQ=", sb.toString());
        assertEquals(-1, is.read());
        is.close();
    }

    @Test(timeout = 4000)
    public void testDecodeChunkRead() throws IOException {
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);

        byte[] dest = new byte[HELLO_WORLD_BYTES.length];
        int totalRead = 0;
        int bytesRead;
        while ((bytesRead = is.read(dest, totalRead, dest.length - totalRead)) != -1) {
            totalRead += bytesRead;
            if (totalRead == dest.length) {
                break;
            }
        }
        assertEquals(HELLO_WORLD_BYTES.length, totalRead);
        assertArrayEquals(HELLO_WORLD_BYTES, dest);
        assertEquals(-1, is.read(dest, 0, dest.length));
        is.close();
    }

    @Test(timeout = 4000)
    public void testEncodeChunkRead() throws IOException {
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_BYTES);
        BaseNCodecInputStream is = new Base64InputStream(in, true);

        byte[] dest = new byte[100];
        int readLen = is.read(dest, 0, dest.length);
        assertTrue(readLen > 0);
        byte[] result = Arrays.copyOf(dest, readLen);
        assertArrayEquals(HELLO_WORLD_B64, result);
        assertEquals(-1, is.read(dest, 0, dest.length));
        is.close();
    }

    @Test(timeout = 4000)
    public void testReadUnsignedBytePromotion() throws IOException {
        // High byte (128 to 255) to verify "b < 0 ? 256 + b : b" logic
        byte[] input = new byte[]{(byte) 0xFF, (byte) 0x80, 0x00, 0x7F};
        // Encode manually or decode from its base64 form
        Base64 codec = new Base64();
        byte[] encoded = codec.encode(input);

        InputStream in = new ByteArrayInputStream(encoded);
        BaseNCodecInputStream is = new Base64InputStream(in, false);

        assertEquals(255, is.read());
        assertEquals(128, is.read());
        assertEquals(0, is.read());
        assertEquals(127, is.read());
        assertEquals(-1, is.read());
        is.close();
    }

    @Test(timeout = 4000)
    public void testMarkSupportedIsFalse() {
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);
        assertFalse(is.markSupported());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadZeroLength() throws IOException {
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);

        byte[] buffer = new byte[10];
        int read = is.read(buffer, 0, 0);
        assertEquals(0, read);
        is.close();
    }

    @Test(timeout = 4000)
    public void testReadEmptyStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        BaseNCodecInputStream is = new Base64InputStream(in);

        assertEquals(-1, is.read());
        assertEquals(-1, is.read(new byte[10], 0, 10));
        is.close();
    }

    @Test(timeout = 4000)
    public void testReadWithOffset() throws IOException {
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);

        byte[] dest = new byte[20];
        int offset = 5;
        int readLen = is.read(dest, offset, HELLO_WORLD_BYTES.length);
        assertEquals(HELLO_WORLD_BYTES.length, readLen);

        byte[] extracted = new byte[readLen];
        System.arraycopy(dest, offset, extracted, 0, readLen);
        assertArrayEquals(HELLO_WORLD_BYTES, extracted);
        is.close();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Codec-130, skip, available)
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSkipWrongArgumentThrowsException() throws IOException {
        // Defects4J failure: skip(-1) must throw IllegalArgumentException
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);
        try {
            is.skip(-1);
        } finally {
            is.close();
        }
    }

    @Test(timeout = 4000)
    public void testCodec130SkipInterleavedWithRead() throws IOException {
        // Defects4J failure: testCodec130
        // Skipping 1 byte after reading 1 byte should skip decoded 'e', leaving "llo World"
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);

        assertEquals('H', is.read());
        assertEquals(1L, is.skip(1));
        assertEquals('l', is.read());
        assertEquals('l', is.read());
        assertEquals('o', is.read());

        byte[] remaining = new byte[HELLO_WORLD_BYTES.length - 5];
        int read = is.read(remaining, 0, remaining.length);
        assertEquals(remaining.length, read);
        assertEquals(" World", StringUtils.newStringUtf8(remaining));
        is.close();
    }

    @Test(timeout = 4000)
    public void testSkipZero() throws IOException {
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);

        assertEquals(0L, is.skip(0));
        assertEquals('H', is.read());
        is.close();
    }

    @Test(timeout = 4000)
    public void testSkipPastEnd() throws IOException {
        // Defects4J failure: testSkipPastEnd / testSkipBig
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);

        long skipped = is.skip(HELLO_WORLD_BYTES.length + 100);
        assertEquals(HELLO_WORLD_BYTES.length, skipped);
        assertEquals(-1, is.read());
        is.close();
    }

    @Test(timeout = 4000)
    public void testSkipToEnd() throws IOException {
        // Defects4J failure: testSkipToEnd
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);

        long skipped = is.skip(HELLO_WORLD_BYTES.length);
        assertEquals(HELLO_WORLD_BYTES.length, skipped);
        assertEquals(-1, is.read());
        is.close();
    }

    @Test(timeout = 4000)
    public void testAvailableContract() throws IOException {
        // Defects4J failure: testAvailable (must return 1 before EOF and 0 at EOF)
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);

        assertEquals(1, is.available());
        while (is.read() != -1) {
            // consume stream
        }
        assertEquals(0, is.available());
        is.close();
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testReadNullBuffer() throws IOException {
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);
        try {
            is.read(null, 0, 1);
        } finally {
            is.close();
        }
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadNegativeOffset() throws IOException {
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);
        try {
            is.read(new byte[10], -1, 1);
        } finally {
            is.close();
        }
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadNegativeLen() throws IOException {
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);
        try {
            is.read(new byte[10], 0, -1);
        } finally {
            is.close();
        }
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadOffsetGreaterThanLength() throws IOException {
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);
        try {
            is.read(new byte[10], 11, 0);
        } finally {
            is.close();
        }
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadOffsetPlusLenGreaterThanLength() throws IOException {
        InputStream in = new ByteArrayInputStream(HELLO_WORLD_B64);
        BaseNCodecInputStream is = new Base64InputStream(in);
        try {
            is.read(new byte[10], 8, 3);
        } finally {
            is.close();
        }
    }

    // =========================================================================
    // Partition E: Direct Subclassing & Large Data Buffering
    // =========================================================================

    @Test(timeout = 4000)
    public void testDirectSubclassEncodingAndDecoding() throws IOException {
        byte[] inputData = new byte[10000];
        for (int i = 0; i < inputData.length; i++) {
            inputData[i] = (byte) (i % 128);
        }

        // Test direct instantiation with doEncode = true
        BaseNCodec codecEnc = new Base64();
        BaseNCodecInputStream encStream = new BaseNCodecInputStream(new ByteArrayInputStream(inputData), codecEnc, true) {};

        // Read all encoded data
        byte[] encBuffer = new byte[16384];
        int encOffset = 0;
        int r;
        while ((r = encStream.read(encBuffer, encOffset, encBuffer.length - encOffset)) != -1) {
            encOffset += r;
            if (encOffset == encBuffer.length) {
                encBuffer = Arrays.copyOf(encBuffer, encBuffer.length * 2);
            }
        }
        encStream.close();

        byte[] encodedBytes = Arrays.copyOf(encBuffer, encOffset);

        // Test direct instantiation with doEncode = false
        BaseNCodec codecDec = new Base64();
        BaseNCodecInputStream decStream = new BaseNCodecInputStream(new ByteArrayInputStream(encodedBytes), codecDec, false) {};

        byte[] decBuffer = new byte[16384];
        int decOffset = 0;
        while ((r = decStream.read(decBuffer, decOffset, decBuffer.length - decOffset)) != -1) {
            decOffset += r;
            if (decOffset == decBuffer.length) {
                decBuffer = Arrays.copyOf(decBuffer, decBuffer.length * 2);
            }
        }
        decStream.close();

        byte[] decodedBytes = Arrays.copyOf(decBuffer, decOffset);
        assertArrayEquals(inputData, decodedBytes);
    }
}