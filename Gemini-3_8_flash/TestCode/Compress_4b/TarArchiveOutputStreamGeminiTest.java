/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Class Under Test: org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
 *
 * Targeted Decision Branches & Boundaries:
 * 1. Constructors:
 *    - (os) -> uses default block size (10240) and record size (512)
 *    - (os, blockSize) -> uses custom blockSize and default record size (512)
 *    - (os, blockSize, recordSize) -> custom block and record sizes
 * 2. Long File Handling (entry.getName().length() >= 100):
 *    - LONGFILE_ERROR: throws RuntimeException when name >= 100.
 *    - LONGFILE_TRUNCATE: allows writing without long link header, truncates name.
 *    - LONGFILE_GNU: writes GNU LongLink entry first, then actual entry; verifies full roundtrip name.
 * 3. Archive Entry Types:
 *    - Non-TarArchiveEntry: throws ClassCastException on putArchiveEntry.
 *    - Directory entry: currSize forced to 0 regardless of entry.getSize().
 *    - Regular file: currSize reflects entry.getSize().
 * 4. Write & Buffer Assembly Pipeline:
 *    - Buffer overflow guard: (currBytes + numToWrite) > currSize throws IOException.
 *    - assemLen > 0 and (assemLen + numToWrite) < recordBuf.length: accumulates into assemBuf.
 *    - assemLen > 0 and (assemLen + numToWrite) >= recordBuf.length: fills recordBuf, flushes, remaining data loop.
 *    - numToWrite >= recordBuf.length: writes complete records directly.
 *    - numToWrite < recordBuf.length: fragments stored in assemBuf for next cycle or close.
 * 5. Entry Lifecycle & Premature Closures:
 *    - closeArchiveEntry() with pending assemLen > 0: pads record with zeros, flushes to buffer.
 *    - closeArchiveEntry() when currBytes < currSize: throws premature close IOException.
 *    - finish() when haveUnclosedEntry == true: throws unclosed entries IOException.
 *    - Normal finish() -> writes two 512-byte zero EOF records.
 *    - close() idempotent behavior: second close() does nothing after stream is closed.
 * 6. File & Metadata Operations:
 *    - createArchiveEntry(File, String) -> creates TarArchiveEntry with proper bindings.
 *    - flush() -> delegates to underlying OutputStream.
 *    - getRecordSize() -> returns accurate record size from TarBuffer.
 */
public class TarArchiveOutputStreamGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & Constructors
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndRecordSize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        assertEquals("Default record size must be 512 bytes", TarBuffer.DEFAULT_RCDSIZE, taos.getRecordSize());
        taos.close();
    }

    @Test(timeout = 4000)
    public void testCustomBlockAndRecordSizeConstructors() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taosBlock = new TarArchiveOutputStream(baos, 1024);
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, taosBlock.getRecordSize());
        taosBlock.close();

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        TarArchiveOutputStream taosCustom = new TarArchiveOutputStream(baos2, 1024, 512);
        assertEquals(512, taosCustom.getRecordSize());
        taosCustom.close();
    }

    @Test(timeout = 4000)
    public void testStandardFileWriteAndReadRoundtrip() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);

        String entryName = "test-file.txt";
        byte[] content = "Hello, Tar World!".getBytes("UTF-8");

        TarArchiveEntry entry = new TarArchiveEntry(entryName);
        entry.setSize(content.length);

        taos.putArchiveEntry(entry);
        taos.write(content, 0, content.length);
        taos.closeArchiveEntry();
        taos.finish();
        taos.close();

        byte[] tarBytes = baos.toByteArray();
        assertTrue("Tar archive should not be empty", tarBytes.length > 0);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull("First entry should exist", readEntry);
        assertEquals(entryName, readEntry.getName());
        assertEquals(content.length, readEntry.getSize());

        byte[] readContent = new byte[content.length];
        int readBytes = tais.read(readContent);
        assertEquals(content.length, readBytes);
        assertArrayEquals(content, readContent);

        assertNull("Should be end of archive", tais.getNextTarEntry());
        tais.close();
    }

    @Test(timeout = 4000)
    public void testDirectoryEntryHandling() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);

        TarArchiveEntry dirEntry = new TarArchiveEntry("testdir/");
        dirEntry.setSize(100); // Intentionally set size on directory to test override
        taos.putArchiveEntry(dirEntry);
        // Directory entries should reset currSize to 0 internally
        taos.closeArchiveEntry();

        taos.finish();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertTrue("Entry must be recognized as directory", readEntry.isDirectory());
        assertEquals("testdir/", readEntry.getName());
        tais.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Buffer Assembly Logic
    // =========================================================================

    @Test(timeout = 4000)
    public void testAssemblyBufferSmallChunksAcrossRecordBoundary() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos, 1024, 512);

        int totalSize = 1200;
        byte[] payload = new byte[totalSize];
        for (int i = 0; i < totalSize; i++) {
            payload[i] = (byte) (i % 127);
        }

        TarArchiveEntry entry = new TarArchiveEntry("chunked.dat");
        entry.setSize(totalSize);
        taos.putArchiveEntry(entry);

        // Write in small chunks to exercise assembly buffer branch:
        // assemLen > 0 && (assemLen + numToWrite) < recordBuf.length
        // assemLen > 0 && (assemLen + numToWrite) >= recordBuf.length
        int chunkSize = 73;
        int written = 0;
        while (written < totalSize) {
            int toWrite = Math.min(chunkSize, totalSize - written);
            taos.write(payload, written, toWrite);
            written += toWrite;
        }

        taos.closeArchiveEntry();
        taos.finish();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals(totalSize, readEntry.getSize());

        byte[] restored = new byte[totalSize];
        int offset = 0;
        while (offset < totalSize) {
            int r = tais.read(restored, offset, totalSize - offset);
            if (r == -1) break;
            offset += r;
        }
        assertEquals(totalSize, offset);
        assertArrayEquals(payload, restored);
        tais.close();
    }

    @Test(timeout = 4000)
    public void testWriteLargeDirectRecordBufferAndTrailingPartial() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);

        // Write larger than 512 bytes at once
        int totalSize = 1500;
        byte[] payload = new byte[totalSize];
        for (int i = 0; i < totalSize; i++) {
            payload[i] = (byte) ((i * 3) % 256);
        }

        TarArchiveEntry entry = new TarArchiveEntry("large.bin");
        entry.setSize(totalSize