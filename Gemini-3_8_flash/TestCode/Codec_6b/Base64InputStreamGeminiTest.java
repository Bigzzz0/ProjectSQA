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
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target: org.apache.commons.codec.binary.Base64InputStream
 * 
 * 1. Constructor Branches:
 *    - Base64InputStream(in) -> delegates to (in, false)
 *    - Base64InputStream(in, doEncode) -> initializes Base64(false)
 *    - Base64InputStream(in, doEncode, lineLength, lineSeparator) -> custom line specs
 *
 * 2. Method read():
 *    - r == 0 loop handling (busy wait / retry on 0-length reads)
 *    - r > 0 with singleByte[0] < 0 (negative byte promotion to unsigned 0..255)
 *    - r > 0 with singleByte[0] >= 0 (standard byte value)
 *    - r <= 0 returning -1 (EOF)
 *
 * 3. Method read(byte[] b, int offset, int len):
 *    - b == null -> NullPointerException
 *    - offset < 0 || len < 0 -> IndexOutOfBoundsException
 *    - offset > b.length || offset + len > b.length -> IndexOutOfBoundsException
 *    - len == 0 -> early return 0 without stream interaction
 *    - !base64.hasData() -> buffer allocation (4096 if doEncode else 8192)
 *    - in.read(buf) == c
 *    - optimization branch: (c > 0 && b.length == len) -> base64.setInitialBuffer(b, offset, len)
 *    - non-optimized branch: !(c > 0 && b.length == len)
 *    - doEncode == true -> base64.encode(buf, 0, c)
 *    - doEncode == false -> base64.decode(buf, 0, c)
 *    - base64.readResults(b, offset, len)
 *
 * 4. Defect CODEC-101 (Defects4J Ground Truth):
 *    - Problem: read(byte[], int, int) returned 0 when reading non-base64 characters / whitespace
 *               instead of looping until data or EOF (-1) is returned.
 *    - Failure: junit.framework.AssertionFailedError: Codec101: First read successful [c=0]
 * ---------------------------------------------------------------------------------------------------------
 */
public class Base64InputStreamGeminiTest {

    private static final String ENCODED_HELLO_WORLD = "SGVsbG8gV29ybGQ=";
    private static final String DECODED_HELLO_WORLD = "Hello World";

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDecodeSingleByteRead() throws IOException {
        byte[] input = ENCODED_HELLO_WORLD.getBytes("UTF-8");
        InputStream b64In = new Base64InputStream(new ByteArrayInputStream(input));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int b;
        while ((b = b64In.read()) != -1) {
            out.write(b);
        }
        b64In.close();

        assertEquals(DECODED_HELLO_WORLD, new String(out.toByteArray(), "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testEncodeSingleByteRead() throws IOException {
        byte[] input = DECODED_HELLO_WORLD.getBytes("UTF-8");
        InputStream b64In = new Base64InputStream(new ByteArrayInputStream(input), true, 0, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int b;
        while ((b = b64In.read()) != -1) {
            out.write(b);
        }
        b64In.close();

        assertEquals(ENCODED_HELLO_WORLD, new String(out.toByteArray(), "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testDecodeBufferOptimizedPath() throws IOException {
        // Triggers (c > 0 && b.length == len) optimization in read(byte[], int, int)
        byte[] input = ENCODED_HELLO_WORLD.getBytes("UTF-8");
        InputStream b64In = new Base64InputStream(new ByteArrayInputStream(input));

        byte[] dest = new byte[11]; // Exactly matches "Hello World" length
        int bytesRead = b64In.read(dest, 0, dest.length);

        assertEquals(11, bytesRead);
        assertEquals(DECODED_HELLO_WORLD, new String(dest, "UTF-8"));
        assertEquals(-1, b64In.read(dest, 0, dest.length));
        b64In.close();
    }

    @Test(timeout = 4000)
    public void testDecodeBufferNonOptimizedPath() throws IOException {
        // Triggers branch where b.length != len
        byte[] input = ENCODED_HELLO_WORLD.getBytes("UTF-8");
        InputStream b64In = new Base64InputStream(new ByteArrayInputStream(input));

        byte[] dest = new byte[32];
        int bytesRead = b64In.read(dest, 5, 11);

        assertEquals(11, bytesRead);
        assertEquals(DECODED_HELLO_WORLD, new String(dest, 5, 11, "UTF-8"));
        b64In.close();
    }

    @Test(timeout = 4000)
    public void testEncodeWithChunkingAndLineSeparator() throws IOException {
        byte[] input = "01234567890123456789".getBytes("UTF-8");
        byte[] separator = new byte[]{'\r', '\n'};
        InputStream b64In = new Base64InputStream(new ByteArrayInputStream(input), true, 4, separator);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8];
        int read;
        while ((read = b64In.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, read);
        }
        b64In.close();

        String expected = "MDEy\r\nMzQ1\r\nNjc4\r\nOTAx\r\nMjM0\r\nNTY3\r\nODk=\r\n";
        assertEquals(expected, new String(out.toByteArray(), "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testLargeDataStreamEncodeDecode() throws IOException {
        // Verifies handling data larger than internal 4096/8192 buffers
        byte[] original = new byte[16384];
        for (int i = 0; i < original.length; i++) {
            original[i] = (byte) (i % 127);
        }

        // Encode stream
        Base64InputStream encoderIn = new Base64InputStream(new ByteArrayInputStream(original), true, 0, null);
        ByteArrayOutputStream encodedOut = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = encoderIn.read(buffer)) != -1) {
            encodedOut.write(buffer, 0, len);
        }
        encoderIn.close();

        // Decode stream
        Base64InputStream decoderIn = new Base64InputStream(new ByteArrayInputStream(encodedOut.toByteArray()));
        ByteArrayOutputStream decodedOut = new ByteArrayOutputStream();
        while ((len = decoderIn.read(buffer)) != -1) {
            decodedOut.write(buffer, 0, len);
        }
        decoderIn.close();

        assertArrayEquals(original, decodedOut.toByteArray());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyStreamDecode() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        assertEquals(-1, in.read());
        byte[] buf = new byte[10];
        assertEquals(-1, in.read(buf, 0, 10));
        in.close();
    }

    @Test(timeout = 4000)
    public void testEmptyStreamEncode() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]), true);
        assertEquals(-1, in.read());
        byte[] buf = new byte[10];
        assertEquals(-1, in.read(buf, 0, 10));
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadZeroLength() throws IOException {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(ENCODED_HELLO_WORLD.getBytes("UTF-8")));
        byte[] buf = new byte[10];
        assertEquals(0, in.read(buf, 0, 0));
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadNegativeBytePromotion() throws IOException {
        // Base64 "/w==" decodes to 0xFF (-1 in signed byte, 255 unsigned)
        // Tests the ternary: singleByte[0] < 0 ? 256 + singleByte[0] : singleByte[0]
        byte[] input = "/w==".getBytes("UTF-8");
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(input));

        int val = in.read();
        assertEquals(255, val);
        assertEquals(-1, in.read());
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadPositiveByteNoPromotion() throws IOException {
        // Base64 "QQ==" decodes to 'A' (65)
        byte[] input = "QQ==".getBytes("UTF-8");
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(input));

        int val = in.read();
        assertEquals(65, val);
        assertEquals(-1, in.read());
        in.close();
    }

    @Test(timeout = 4000)
    public void testMarkSupportedReturnsFalse() {
        Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(new byte[0]));
        assertFalse(in.markSupported());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch