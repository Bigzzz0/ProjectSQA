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
package org.apache.commons.compress.utils;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Classes Tested:
 *   - org.apache.commons.compress.utils.ChecksumCalculatingInputStream
 *
 * Targeted Decision Branches & Conditions:
 *   1. Constructor Defect Validation (Defects4J ground truth):
 *      - Checksum parameter is null -> NullPointerException expected.
 *      - InputStream parameter is null -> NullPointerException expected.
 *      - Both parameters are null -> NullPointerException expected.
 *   2. read():
 *      - ret >= 0 branch: updates checksum, returns byte value.
 *      - ret < 0 branch (EOF): does not update checksum, returns -1.
 *   3. read(byte[]):
 *      - Delegates correctly to read(byte[], 0, byte[].length).
 *   4. read(byte[], int, int):
 *      - ret >= 0 branch: updates checksum with (b, off, ret), returns count.
 *      - ret < 0 branch (EOF): does not update checksum, returns -1.
 *      - ret == 0 boundary (len == 0): ret is 0, updates 0 bytes.
 *   5. skip(long):
 *      - read() >= 0 branch: returns 1 byte skipped (consuming data to update checksum).
 *      - read() < 0 branch (EOF): returns 0.
 *   6. getValue():
 *      - Verifies accurate delegation to underlying Checksum#getValue().
 *   7. Exception Propagation:
 *      - read() & read(b, off, len) transparently propagate IOException from underlying stream.
 */
public class ChecksumCalculatingInputStreamGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testClassInstantiationWithNullChecksumThrowsNullPointerException() {
        // Targets defect: missing null-check for Checksum in constructor
        final InputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        new ChecksumCalculatingInputStream(null, in);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testClassInstantiationWithNullInputStreamThrowsNullPointerException() {
        // Targets defect: missing null-check for InputStream in constructor
        final Checksum checksum = new CRC32();
        new ChecksumCalculatingInputStream(checksum, null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testClassInstantiationWithBothParametersNullThrowsNullPointerException() {
        // Targets defect: missing null-check when both parameters are null
        new ChecksumCalculatingInputStream(null, null);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadSingleByteUntilEofCalculatesChecksumCorrectly() throws IOException {
        final byte[] data = new byte[]{'A', 'B', 'C', 'D'};
        final CRC32 expectedChecksum = new CRC32();
        expectedChecksum.update(data);

        final CRC32 actualChecksum = new CRC32();
        try (final ChecksumCalculatingInputStream stream =
                     new ChecksumCalculatingInputStream(actualChecksum, new ByteArrayInputStream(data))) {

            for (byte b : data) {
                final int readByte = stream.read();
                assertEquals((int) (b & 0xFF), readByte);
            }
            final int eof = stream.read();
            assertEquals(-1, eof);

            assertEquals(expectedChecksum.getValue(), stream.getValue());
            assertEquals(expectedChecksum.getValue(), actualChecksum.getValue());
        }
    }

    @Test(timeout = 4000)
    public void testReadByteArrayCalculatesChecksumCorrectly() throws IOException {
        final byte[] data = "Defects4J White-Box Testing".getBytes();
        final Adler32 expectedChecksum = new Adler32();
        expectedChecksum.update(data);

        final Adler32 actualChecksum = new Adler32();
        try (final ChecksumCalculatingInputStream stream =
                     new ChecksumCalculatingInputStream(actualChecksum, new ByteArrayInputStream(data))) {

            final byte[] buffer = new byte[data.length];
            final int bytesRead = stream.read(buffer);

            assertEquals(data.length, bytesRead);
            assertArrayEquals(data, buffer);
            assertEquals(expectedChecksum.getValue(), stream.getValue());

            // Reading past EOF returns -1 and does not alter checksum
            final int eof = stream.read(buffer);
            assertEquals(-1, eof);
            assertEquals(expectedChecksum.getValue(), stream.getValue());
        }
    }

    @Test(timeout = 4000)
    public void testReadByteArrayWithOffsetAndLength() throws IOException {
        final byte[] data = new byte[]{10, 20, 30, 40, 50, 60, 70, 80};
        final CRC32 expectedChecksum = new CRC32();
        // We will read in two chunks: 3 bytes, then 5 bytes
        expectedChecksum.update(data, 0, 3);
        expectedChecksum.update(data, 3, 5);

        final CRC32 actualChecksum = new CRC32();
        try (final ChecksumCalculatingInputStream stream =
                     new ChecksumCalculatingInputStream(actualChecksum, new ByteArrayInputStream(data))) {

            final byte[] buffer = new byte[16];
            final int chunk1 = stream.read(buffer, 2, 3);
            assertEquals(3, chunk1);
            for (int i = 0; i < 3; i++) {
                assertEquals(data[i], buffer[2 + i]);
            }

            final int chunk2 = stream.read(buffer, 5, 5);
            assertEquals(5, chunk2);
            for (int i = 0; i < 5; i++) {
                assertEquals(data[3 + i], buffer[5 + i]);
            }

            assertEquals(-1, stream.read(buffer, 0, 1));
            assertEquals(expectedChecksum.getValue(), stream.getValue());
        }
    }

    @Test(timeout = 4000)
    public void testSkipAdvancesStreamAndUpdatesChecksum() throws IOException {
        final byte[] data = new byte[]{1, 2, 3};
        final CRC32 expectedChecksum = new CRC32();
        expectedChecksum.update(1);
        expectedChecksum.update(2);

        final CRC32 actualChecksum = new CRC32();
        try (final ChecksumCalculatingInputStream stream =
                     new ChecksumCalculatingInputStream(actualChecksum, new ByteArrayInputStream(data))) {

            // skip(long) contract in ChecksumCalculatingInputStream reads 1 byte and returns 1
            final long skippedFirst = stream.skip(100L);
            assertEquals(1L, skippedFirst);

            final long skippedSecond = stream.skip(1L);
            assertEquals(1L, skippedSecond);

            assertEquals(expectedChecksum.getValue(), stream.getValue());

            final int third = stream.read();
            assertEquals(3, third);

            final long skippedAtEof = stream.skip(1L);
            assertEquals(0L, skippedAtEof);
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyInputStreamImmediateEof() throws IOException {
        final CRC32 checksum = new CRC32();
        final long initialChecksumValue = checksum.getValue();

        try (final ChecksumCalculatingInputStream stream =
                     new ChecksumCalculatingInputStream(checksum, new ByteArrayInputStream(new byte[0]))) {

            assertEquals(-1, stream.read());
            assertEquals(initialChecksumValue, stream.getValue());

            final byte[] buffer = new byte[10];
            assertEquals(-1, stream.read(buffer));
            assertEquals(-1, stream.read(buffer, 0, buffer.length));
            assertEquals(0L, stream.skip(5L));
            assertEquals(initialChecksumValue, stream.getValue());
        }
    }

    @Test(timeout = 4000)
    public void testReadZeroBytesBuffer() throws IOException {
        final byte[] data = new byte[]{42};
        final CRC32 checksum = new CRC32();
        try (final ChecksumCalculatingInputStream stream =
                     new ChecksumCalculatingInputStream(checksum, new ByteArrayInputStream(data))) {

            final byte[] emptyBuffer = new byte[0];
            final int readEmpty = stream.read(emptyBuffer);
            assertEquals(0, readEmpty);
            assertEquals(0L, stream.getValue());

            final byte[] buffer = new byte[5];
            final int readZeroLen = stream.read(buffer, 0, 0);
            assertEquals(0, readZeroLen);
            assertEquals(0L, stream.getValue());

            // The data should still be intact in stream
            final int val = stream.read();
            assertEquals(42, val);
            assertNotEquals(0L, stream.getValue());
        }
    }

    @Test(timeout = 4000)
    public void testSkipOnEmptyStreamReturnsZero() throws IOException {
        final CRC32 checksum = new CRC32();
        try (final ChecksumCalculatingInputStream stream =
                     new ChecksumCalculatingInputStream(checksum, new ByteArrayInputStream(new byte[0]))) {

            assertEquals(0L, stream.skip(0L));
            assertEquals(0L, stream.skip(1L));
            assertEquals(0L, stream.skip(-1L));
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IOException.class, timeout = 4000)
    public void testReadPropagatesUnderlyingIOException() throws IOException {
        final InputStream brokenStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated underlying stream read failure");
            }
        };

        try (final ChecksumCalculatingInputStream stream =
                     new ChecksumCalculatingInputStream(new CRC32(), brokenStream)) {
            stream.read();
        }
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testReadByteArrayPropagatesUnderlyingIOException() throws IOException {
        final InputStream brokenStream = new InputStream() {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("Simulated underlying stream read array failure");
            }

            @Override
            public int read() throws IOException {
                return -1;
            }
        };

        try (final ChecksumCalculatingInputStream stream =
                     new ChecksumCalculatingInputStream(new CRC32(), brokenStream)) {
            stream.read(new byte[8], 0, 8);
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetValueIsIdempotentWithoutReads() {
        final CRC32 checksum = new CRC32();
        checksum.update(99);
        final long expected = checksum.getValue();

        final ChecksumCalculatingInputStream stream =
                new ChecksumCalculatingInputStream(checksum, new ByteArrayInputStream(new byte[]{1, 2, 3}));

        assertEquals(expected, stream.getValue());
        assertEquals(expected, stream.getValue());
    }

    @Test(timeout = 4000)
    public void testInterleavedReadSingleAndBulk() throws IOException {
        final byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7};
        final CRC32 expectedChecksum = new CRC32();
        expectedChecksum.update(data);

        final CRC32 actualChecksum = new CRC32();
        try (final ChecksumCalculatingInputStream stream =
                     new ChecksumCalculatingInputStream(actualChecksum, new ByteArrayInputStream(data))) {

            // Read 1 byte
            assertEquals(1, stream.read());
            // Read 2 bytes via array
            final byte[] chunk = new byte[2];
            assertEquals(2, stream.read(chunk));
            assertEquals(2, chunk[0]);
            assertEquals(3, chunk[1]);
            // Skip 1 byte (consumes byte 4)
            assertEquals(1L, stream.skip(10L));
            // Read remainder
            final byte[] remaining = new byte[4];
            final int readCount = stream.read(remaining, 0, remaining.length);
            assertEquals(3, readCount); // bytes 5, 6, 7
            assertEquals(5, remaining[0]);
            assertEquals(6, remaining[1]);
            assertEquals(7, remaining[2]);

            assertEquals(-1, stream.read());
            assertEquals(expectedChecksum.getValue(), stream.getValue());
        }
    }
}