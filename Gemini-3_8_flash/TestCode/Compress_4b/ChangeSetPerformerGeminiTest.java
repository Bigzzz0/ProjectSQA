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
package org.apache.commons.compress.changes;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import java.util.zip.ZipEntry;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.compress.changes.ChangeSetPerformer
 * Target Operations:
 *   - Constructor: ChangeSetPerformer(ChangeSet) -> extracts Set via getChanges()
 *   - perform(ArchiveInputStream in, ArchiveOutputStream out):
 *       * Phase 1 (Initial additions with replace mode):
 *           - Branch: change.type() == TYPE_ADD && isReplaceMode()
 *           - Result: entry written to out, removed from workingSet, addedFromChangeSet()
 *       * Phase 2 (Input stream reading & deletion filtering):
 *           - Branch: entry != null (iteration)
 *           - Branch: TYPE_DELETE && name != null:
 *               - Branch: name.equals(targetFile) -> copy=false, remove change, deleted(name), break
 *               - Branch: name does not match -> continues
 *           - Branch: TYPE_DELETE_DIR && name != null:
 *               - Branch: name.startsWith(targetFile + "/") -> copy=false, deleted(name), break (not removed)
 *               - Branch: name does not match prefix -> continues
 *           - Branch: copy == true:
 *               - Sub-branch: !isDeletedLater(...) && !hasBeenAdded(name) -> copyStream, addedFromStream
 *               - Sub-branch: isDeletedLater is true -> skipped
 *               - Sub-branch: hasBeenAdded is true -> skipped (avoid duplicate entry)
 *       * Phase 3 (Final additions without replace mode):
 *           - Branch: TYPE_ADD && !isReplaceMode() && !hasBeenAdded(name) -> copyStream, addedFromChangeSet
 *           - Branch: TYPE_ADD && !isReplaceMode() && hasBeenAdded(name) -> skipped
 *       * Helper isDeletedLater:
 *           - Branch: workingSet.isEmpty() -> false
 *           - Branch: TYPE_DELETE && source.equals(target) -> true
 *           - Branch: TYPE_DELETE_DIR && source.startsWith(target + "/") -> true
 *           - Branch: Exhausted loop -> false
 *       * Helper copyStream:
 *           - Invokes putArchiveEntry, IOUtils.copy, closeArchiveEntry
 *
 * Ground Truth Defect Analysis:
 *   - Defect: JarArchiveOutputStream and ZipArchiveOutputStream corrupting archives when no entries
 *     or special markers are written, causing "central directory is empty, can't expand corrupt archive".
 *   - Targeted in Partition C by executing ChangeSetPerformer with JarArchiveOutputStream and ZipFile.
 */
public class ChangeSetPerformerGeminiTest {

    // =========================================================================
    // Test Doubles (Custom Test Archive Streams for Clean In-Memory Testing)
    // =========================================================================

    private static class SimpleArchiveEntry implements ArchiveEntry {
        private final String name;
        private final long size;

        SimpleArchiveEntry(String name, long size) {
            this.name = name;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public long getSize() {
            return size;
        }

        public boolean isDirectory() {
            return name != null && name.endsWith("/");
        }

        public java.util.Date getLastModifiedDate() {
            return new java.util.Date();
        }
    }

    private static class MockArchiveInputStream extends ArchiveInputStream {
        private final ArchiveEntry[] entries;
        private final byte[][] data;
        private int index = -1;
        private ByteArrayInputStream currentStream = null;

        MockArchiveInputStream(ArchiveEntry[] entries, byte[][] data) {
            this.entries = entries;
            this.data = data;
        }

        public ArchiveEntry getNextEntry() throws IOException {
            index++;
            if (entries != null && index < entries.length) {
                if (data != null && index < data.length) {
                    currentStream = new ByteArrayInputStream(data[index]);
                } else {
                    currentStream = new ByteArrayInputStream(new byte[0]);
                }
                return entries[index];
            }
            return null;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (currentStream != null) {
                return currentStream.read(b, off, len);
            }
            return -1;
        }
    }

    private static class MockArchiveOutputStream extends ArchiveOutputStream {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private ArchiveEntry currentEntry;
        private int putCount = 0;
        private int closeEntryCount = 0;
        private boolean finishCalled = false;

        public void putArchiveEntry(ArchiveEntry entry) throws IOException {
            this.currentEntry = entry;
            putCount++;
        }

        public void closeArchiveEntry() throws IOException {
            currentEntry = null;
            closeEntryCount++;
        }

        public void finish() throws IOException {
            finishCalled = true;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            baos.write(b, off, len);
        }

        public void write(int b) throws IOException {
            baos.write(b);
        }

        public byte[] toByteArray() {
            return baos.toByteArray();
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPerformAddOnlyWithReplaceMode() throws Exception {
        ChangeSet cs = new ChangeSet();
        ArchiveEntry entry1 = new SimpleArchiveEntry("file1.txt", 5);
        byte[] content1 = "hello".getBytes("UTF-8");
        cs.add(entry1, new ByteArrayInputStream(content1), true);

        ChangeSetPerformer performer = new ChangeSetPerformer(cs);
        MockArchiveInputStream in = new MockArchiveInputStream(new ArchiveEntry[0], new byte[0][0]);
        MockArchiveOutputStream out = new MockArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);

        assertEquals(1, out.putCount);
        assertEquals(1, out.closeEntryCount);
        assertArrayEquals(content1, out.toByteArray());
        assertTrue(results.getAddedFromChangeSet().contains("file1.txt"));
        assertTrue(results.hasBeenAdded("file1.txt"));
        assertEquals(1, results.getAddedFromChangeSet().size());
        assertEquals(0, results.getAddedFromStream().size());
        assertEquals(0, results.getDeleted().size());
    }

    @Test(timeout = 4000)
    public void testPerformAddWithoutReplaceMode() throws Exception {
        ChangeSet cs = new ChangeSet();
        ArchiveEntry entry = new SimpleArchiveEntry("newfile.txt", 4);
        byte[] content = "test".getBytes("UTF-8");
        cs.add(entry, new ByteArrayInputStream(content), false);

        ChangeSetPerformer performer = new ChangeSetPerformer(cs);
        MockArchiveInputStream in = new MockArchiveInputStream(new ArchiveEntry[0], new byte[0][0]);
        MockArchiveOutputStream out = new MockArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);

        assertEquals(1, out.putCount);
        assertEquals(1, out.closeEntryCount);
        assertArrayEquals(content, out.toByteArray());
        assertTrue(results.getAddedFromChangeSet().contains("newfile.txt"));
        assertEquals(0, results.getAddedFromStream().size());
        assertEquals(0, results.getDeleted().size());
    }

    @Test(timeout = 4000)
    public void testPerformDeleteFileMatchesAndRemovesChange() throws Exception {
        ChangeSet cs = new ChangeSet();
        cs.delete("file_to_del.txt");

        ChangeSetPerformer performer = new ChangeSetPerformer(cs);

        ArchiveEntry[] inEntries = new ArchiveEntry[] {
            new SimpleArchiveEntry("file_to_del.txt", 3),
            new SimpleArchiveEntry("file_to_keep.txt", 4)
        };
        byte[][] data = new byte[][] {
            "del".getBytes("UTF-8"),
            "keep".getBytes("UTF-8")
        };

        MockArchiveInputStream in = new MockArchiveInputStream(inEntries, data);
        MockArchiveOutputStream out = new MockArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);

        assertEquals(1, out.putCount);
        assertEquals(1, out.closeEntryCount);
        assertArrayEquals("keep".getBytes("UTF-8"), out.toByteArray());
        assertTrue(results.getDeleted().contains("file_to_del.txt"));
        assertTrue(results.getAddedFromStream().contains("file_to_keep.txt"));
        assertFalse(results.getAddedFromStream().contains("file_to_del.txt"));
    }

    @Test(timeout = 4000)
    public void testPerformDeleteDirPrefixMatch() throws Exception {
        ChangeSet cs = new ChangeSet();
        cs.deleteDir("folder");

        ChangeSetPerformer performer = new ChangeSetPerformer(cs);

        ArchiveEntry[] inEntries = new ArchiveEntry[] {
            new SimpleArchiveEntry("folder/sub1.txt", 2),
            new SimpleArchiveEntry("folder/sub2.txt", 2),
            new SimpleArchiveEntry("folder_other/file.txt", 2)
        };
        byte[][] data = new byte[][] {
            "11".getBytes("UTF-8"),
            "22".getBytes("UTF-8"),
            "33".getBytes("UTF-8")
        };

        MockArchiveInputStream in = new MockArchiveInputStream(inEntries, data);
        MockArchiveOutputStream out = new MockArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);

        assertEquals(1, out.putCount);
        assertEquals(1, out.closeEntryCount);
        assertArrayEquals("33".getBytes("UTF-8"), out.toByteArray());
        assertTrue(results.getDeleted().contains("folder/sub1.txt"));
        assertTrue(results.getDeleted().contains("folder/sub2.txt"));
        assertTrue(results.getAddedFromStream().contains("folder_other/file.txt"));
    }

    @Test(timeout = 4000)
    public void testReplaceModePreventsStreamCopyOfSameName() throws Exception {
        ChangeSet cs = new ChangeSet();
        ArchiveEntry replaceEntry = new SimpleArchiveEntry("dup.txt", 3);
        byte[] replaceContent = "NEW".getBytes("UTF-8");
        cs.add(replaceEntry, new ByteArrayInputStream(replaceContent), true);

        ChangeSetPerformer performer = new ChangeSetPerformer(cs);

        ArchiveEntry[] inEntries = new ArchiveEntry[] {
            new SimpleArchiveEntry("dup.txt", 3)
        };
        byte[][] data = new byte[][] {
            "OLD".getBytes("UTF-8")
        };

        MockArchiveInputStream in = new MockArchiveInputStream(inEntries, data);
        MockArchiveOutputStream out = new MockArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);

        assertEquals(1, out.putCount);
        assertArrayEquals(replaceContent, out.toByteArray());
        assertTrue(results.getAddedFromChangeSet().contains("dup.txt"));
        assertFalse(results.getAddedFromStream().contains("dup.txt"));
    }

    @Test(timeout = 4000)
    public void testNonReplaceModeDoesNotOverwriteStreamEntry() throws Exception {
        ChangeSet cs = new ChangeSet();
        ArchiveEntry addEntry = new SimpleArchiveEntry("dup.txt", 3);
        byte[] addContent = "NEW".getBytes("UTF-8");
        cs.add(addEntry, new ByteArrayInputStream(addContent), false);

        ChangeSetPerformer performer = new ChangeSetPerformer(cs);

        ArchiveEntry[] inEntries = new ArchiveEntry[] {
            new SimpleArchiveEntry("dup.txt", 3)
        };
        byte[][] data = new byte[][] {
            "OLD".getBytes("UTF-8")
        };

        MockArchiveInputStream in = new MockArchiveInputStream(inEntries, data);
        MockArchiveOutputStream out = new MockArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);

        assertEquals(1, out.putCount);
        assertArrayEquals("OLD".getBytes("UTF-8"), out.toByteArray());
        assertTrue(results.getAddedFromStream().contains("dup.txt"));
        assertFalse(results.getAddedFromChangeSet().contains("dup.txt"));
    }

    @Test(timeout = 4000)
    public void testPerformerCanBeReusedMultipleTimes() throws Exception {
        ChangeSet cs = new ChangeSet();
        ArchiveEntry addEntry = new SimpleArchiveEntry("extra.txt", 1);
        cs.add(addEntry, new ByteArrayInputStream("A".getBytes("UTF-8")), false);

        ChangeSetPerformer performer = new ChangeSetPerformer(cs);

        // Run 1
        MockArchiveInputStream in1 = new MockArchiveInputStream(new ArchiveEntry[0], new byte[0][0]);
        MockArchiveOutputStream out1 = new MockArchiveOutputStream();
        ChangeSetResults results1 = performer.perform(in1, out1);
        assertEquals(1, results1.getAddedFromChangeSet().size());

        // Run 2 (verify thread safety/immutability of performer's changeSet)
        MockArchiveInputStream in2 = new MockArchiveInputStream(new ArchiveEntry[0], new byte[0][0]);
        MockArchiveOutputStream out2 = new MockArchiveOutputStream();
        ChangeSetResults results2 = performer.perform(in2, out2);
        assertEquals(1, results2.getAddedFromChangeSet().size());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyChangeSetAndEmptyStream() throws Exception {
        ChangeSet cs = new ChangeSet();
        ChangeSetPerformer performer = new ChangeSetPerformer(cs);

        MockArchiveInputStream in = new MockArchiveInputStream(new ArchiveEntry[0], new byte[0][0]);
        MockArchiveOutputStream out = new MockArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);

        assertNotNull(results);
        assertEquals(0, results.getAddedFromChangeSet().size());
        assertEquals(0, results.getAddedFromStream().size());
        assertEquals(0, results.getDeleted().size());
        assertEquals(0, out.putCount);
    }

    @Test(timeout = 4000)
    public void testNullEntryNameInStreamGracefullyIgnoredInDeletions() throws Exception {
        ChangeSet cs = new ChangeSet();
        cs.delete("any.txt");
        cs.deleteDir("anyDir");

        ChangeSetPerformer performer = new ChangeSetPerformer(cs);

        ArchiveEntry nullNamedEntry = new SimpleArchiveEntry(null, 0);
        MockArchiveInputStream in = new MockArchiveInputStream(new ArchiveEntry[] { nullNamedEntry }, new byte[][] { new byte[0] });
        MockArchiveOutputStream out = new MockArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);

        assertEquals(1, out.putCount);
        assertTrue(results.getAddedFromStream().contains(null));
        assertEquals(0, results.getDeleted().size());
    }

    @Test(timeout = 4000)
    public void testIsDeletedLaterFileAndDir() throws Exception {
        ChangeSet cs = new ChangeSet();
        // Entry added without replace mode
        ArchiveEntry entryA = new SimpleArchiveEntry("toBeDeletedLater.txt", 4);
        cs.add(entryA, new ByteArrayInputStream("data".getBytes("UTF-8")), false);
        // And delete on the same target
        cs.delete("toBeDeletedLater.txt");

        ChangeSetPerformer performer = new ChangeSetPerformer(cs);

        ArchiveEntry streamEntry = new SimpleArchiveEntry("toBeDeletedLater.txt", 4);
        MockArchiveInputStream in = new MockArchiveInputStream(new ArchiveEntry[] { streamEntry }, new byte[][] { "data".getBytes("UTF-8") });
        MockArchiveOutputStream out = new MockArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);

        // streamEntry is deleted by the delete change during Phase 2
        assertEquals(0, out.putCount);
        assertTrue(results.getDeleted().contains("toBeDeletedLater.txt"));
    }

    @Test(timeout = 4000)
    public void testIsDeletedLaterWithDeleteDir() throws Exception {
        ChangeSet cs = new ChangeSet();
        cs.deleteDir("deletedDir");

        ChangeSetPerformer performer = new ChangeSetPerformer(cs);

        ArchiveEntry[] entries = new ArchiveEntry[] {
            new SimpleArchiveEntry("deletedDir/child.txt", 1)
        };
        MockArchiveInputStream in = new MockArchiveInputStream(entries, new byte[][] { "x".getBytes("UTF-8") });
        MockArchiveOutputStream out = new MockArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);

        assertEquals(0, out.putCount);
        assertTrue(results.getDeleted().contains("deletedDir/child.txt"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (Central Directory / Empty Jar / Zip Marker)
    // =========================================================================

    /**
     * Targets the known defect where JarArchiveOutputStream/ZipArchiveOutputStream
     * corrupts the central directory (e.g., "central directory is empty, can't expand corrupt archive").
     * When ChangeSetPerformer processes an empty ChangeSet with a JarArchiveOutputStream,
     * the resulting archive must have a valid structure that ZipFile can parse.
     */
    @Test(timeout = 4000)