package org.apache.commons.compress.changes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Targeted branches and conditions:
 * - perform(): TYPE_ADD with isReplaceMode() true/false
 * - perform(): TYPE_DELETE and TYPE_DELETE_DIR matching conditions
 * - perform(): name != null checks and startsWith boundary
 * - perform(): isDeletedLater() checking TYPE_DELETE/TYPE_DELETE_DIR
 * - isDeletedLater(): empty workingSet, source.equals(target) exact match
 * - isDeletedLater(): source.startsWith(target + "/") directory prefix
 * - copyStream(): putArchiveEntry/copy/closeArchiveEntry sequence
 * - workingSet iteration and removal patterns
 * 
 * Boundary conditions:
 * - Empty streams
 * - Null entry names
 * - Exact path matches vs directory prefix matches
 * - Multiple changes same target
 * - Replace mode vs non-replace mode additions
 * - Deletion then addition in same set
 * - Directory deletion with trailing slash
 * 
 * Defect targeting: The known failures involve "central directory is empty"
 * which suggests issues when the output stream doesn't properly handle
 * the case where all entries are deleted and nothing is written,
 * causing empty Zip archives. This test suite covers scenarios where
 * deletions result in no entries being copied.
 */
public class ChangeSetPerformerDeepseekTest {
    
    // ---- Partition A: Core Functional Logic & State Transitions ----
    
    @Test(timeout = 4000)
    public void testConstructorAndGetChanges() {
        ChangeSet changeSet = new ChangeSet();
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        assertNotNull("Performer should not be null", performer);
        // No getter for changes set, but construction should succeed
    }
    
    @Test(timeout = 4000)
    public void testPerformEmptyChangeSet() throws IOException {
        // Test with empty ChangeSet - all entries should pass through
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        TestArchiveInputStream in = new TestArchiveInputStream(bin);
        TestArchiveOutputStream out = new TestArchiveOutputStream(bout);
        
        // Add some entries to the input stream
        in.addEntry(new TestArchiveEntry("entry1.txt"));
        in.addEntry(new TestArchiveEntry("entry2.txt"));
        
        ChangeSet changeSet = new ChangeSet();
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);
        
        assertEquals("Should have 2 entries from stream", 2, results.getAddedFromStream().size());
        assertEquals("Should have 0 entries from changeset", 0, results.getAddedFromChangeSet().size());
        assertEquals("Should have 0 deleted entries", 0, results.getDeleted().size());
    }
    
    @Test(timeout = 4000)
    public void testPerformSingleDelete() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        TestArchiveInputStream in = new TestArchiveInputStream(bin);
        TestArchiveOutputStream out = new TestArchiveOutputStream(bout);
        
        in.addEntry(new TestArchiveEntry("delete_me.txt"));
        in.addEntry(new TestArchiveEntry("keep_me.txt"));
        
        ChangeSet changeSet = new ChangeSet();
        changeSet.delete("delete_me.txt");
        
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);
        
        assertTrue("delete_me.txt should be deleted", results.getDeleted().contains("delete_me.txt"));
        assertEquals("Should have 1 entry from stream", 1, results.getAddedFromStream().size());
        assertEquals("Entry should be keep_me.txt", "keep_me.txt", results.getAddedFromStream().get(0));
    }
    
    @Test(timeout = 4000)
    public void testPerformDeleteDirectory() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        TestArchiveInputStream in = new TestArchiveInputStream(bin);
        TestArchiveOutputStream out = new TestArchiveOutputStream(bout);
        
        in.addEntry(new TestArchiveEntry("dir/file1.txt"));
        in.addEntry(new TestArchiveEntry("dir/file2.txt"));
        in.addEntry(new TestArchiveEntry("other.txt"));
        
        ChangeSet changeSet = new ChangeSet();
        changeSet.deleteDir("dir");
        
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);
        
        assertEquals("Should have 2 deleted entries", 2, results.getDeleted().size());
        assertTrue("dir/file1.txt should be deleted", results.getDeleted().contains("dir/file1.txt"));
        assertTrue("dir/file2.txt should be deleted", results.getDeleted().contains("dir/file2.txt"));
        assertEquals("Only other.txt should remain", 1, results.getAddedFromStream().size());
    }
    
    @Test(timeout = 4000)
    public void testPerformAddInReplaceMode() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        TestArchiveInputStream in = new TestArchiveInputStream(bin);
        TestArchiveOutputStream out = new TestArchiveOutputStream(bout);
        
        // Add some data for the new entry
        byte[] data = "test data".getBytes();
        ByteArrayInputStream entryData = new ByteArrayInputStream(data);
        
        ChangeSet changeSet = new ChangeSet();
        changeSet.add(entryData, new TestArchiveEntry("new_entry.txt"));
        
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);
        
        assertTrue("new_entry.txt should be added from changeset", results.getAddedFromChangeSet().contains("new_entry.txt"));
        assertEquals("Should have 0 entries from stream", 0, results.getAddedFromStream().size());
    }
    
    @Test(timeout = 4000)
    public void testPerformAddNonReplaceModeWithExistingEntry() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        TestArchiveInputStream in = new TestArchiveInputStream(bin);
        TestArchiveOutputStream out = new TestArchiveOutputStream(bout);
        
        in.addEntry(new TestArchiveEntry("existing.txt"));
        
        byte[] data = "new data".getBytes();
        ByteArrayInputStream entryData = new ByteArrayInputStream(data);
        
        ChangeSet changeSet = new ChangeSet();
        changeSet.add(entryData, new TestArchiveEntry("existing.txt"));
        // When not in replace mode, existing entry from stream should take precedence
        
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);
        
        assertEquals("Existing entry should be from stream", 1, results.getAddedFromStream().size());
        assertEquals("No changeset entries should be added (already exists)", 0, results.getAddedFromChangeSet().size());
    }
    
    // ---- Partition B: Boundary Value Analysis & Extremes ----
    
    @Test(timeout = 4000)
    public void testPerformEmptyInputStream() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        TestArchiveInputStream in = new TestArchiveInputStream(bin);
        TestArchiveOutputStream out = new TestArchiveOutputStream(bout);
        // No entries added to input stream
        
        ChangeSet changeSet = new ChangeSet();
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);
        
        assertEquals("No entries from stream", 0, results.getAddedFromStream().size());
        assertEquals("No entries from changeset", 0, results.getAddedFromChangeSet().size());
        assertEquals("No deleted entries", 0, results.getDeleted().size());
    }
    
    @Test(timeout = 4000)
    public void testPerformNullEntryName() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        TestArchiveInputStream in = new TestArchiveInputStream(bin);
        TestArchiveOutputStream out = new TestArchiveOutputStream(bout);
        
        // Entry with null name
        in.addEntry(new TestArchiveEntry(null));
        
        ChangeSet changeSet = new ChangeSet();
        changeSet.delete("some_file.txt"); // Should not match null name
        
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);
        
        assertEquals("Null-named entry should still pass through", 1, results.getAddedFromStream().size());
        assertNull("Stream entry should have null name", results.getAddedFromStream().get(0));
        assertEquals("No deletions", 0, results.getDeleted().size());
    }
    
    @Test(timeout = 4000)
    public void testIsDeletedLaterExactMatch() throws IOException {
        // Test the isDeletedLater private method behavior through perform
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        TestArchiveInputStream in = new TestArchiveInputStream(bin);
        TestArchiveOutputStream out = new TestArchiveOutputStream(bout);
        
        // Entry that gets deleted later in the changeset
        in.addEntry(new TestArchiveEntry("temp.txt"));
        
        ChangeSet changeSet = new ChangeSet();
        // This delete should mark temp.txt as deleted later when processing
        changeSet.delete("temp.txt");
        
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);
        
        assertEquals("temp.txt should be deleted", 1, results.getDeleted().size());
        assertEquals("No entries from stream (all deleted)", 0, results.getAddedFromStream().size());
    }
    
    @Test(timeout = 4000)
    public void testIsDeletedLaterDirectoryPrefix() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        TestArchiveInputStream in = new TestArchiveInputStream(bin);
        TestArchiveOutputStream out = new TestArchiveOutputStream(bout);
        
        in.addEntry(new TestArchiveEntry("dir/"));
        in.addEntry(new TestArchiveEntry("dir/file.txt"));
        in.addEntry(new TestArchiveEntry("dir/subdir/other.txt"));
        
        ChangeSet changeSet = new ChangeSet();
        changeSet.deleteDir("dir");
        
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);
        
        assertEquals("All dir entries should be deleted", 3, results.getDeleted().size());
    }
    
    // ---- Partition C: Defect-Targeted Branch Zone ----
    
    @Test(timeout = 4000)
    public void testPerformAllEntriesDeleted_DefectScenario() throws IOException {
        // This directly targets the defect scenario where all entries are deleted
        // resulting in an empty output stream that may cause "central directory is empty" errors
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        TestArchiveInputStream in = new TestArchiveInputStream(bin);
        TestArchiveOutputStream out = new TestArchiveOutputStream(bout);
        
        in.addEntry(new TestArchiveEntry("only_entry.txt"));
        
        ChangeSet changeSet = new ChangeSet();
        changeSet.delete("only_entry.txt"); // Delete all entries
        
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);
        
        assertEquals("The only entry should be deleted", 1, results.getDeleted().size());
        assertEquals("No entries should remain from stream", 0, results.getAddedFromStream().size());
        assertEquals("No entries added from changeset", 0, results.getAddedFromChangeSet().size());
        // This completes without error - the defect would manifest downstream
        // when trying to read back the empty archive
    }
    
    @Test(timeout = 4000)
    public void testPerformDeleteThenAddSameName() throws IOException {
        // Scenario: Delete an entry, then add a new one with same name
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        TestArchiveInputStream in = new TestArchiveInputStream(bin);
        TestArchiveOutputStream out = new TestArchiveOutputStream(bout);
        
        in.addEntry(new TestArchiveEntry("conflict.txt"));
        
        // The delete happens first, then we try to add a replacement
        byte[] data = "replacement content".getBytes();
        ByteArrayInputStream newData = new ByteArrayInputStream(data);
        
        ChangeSet changeSet = new ChangeSet();
        changeSet.delete("conflict.txt");
        changeSet.add(newData, new TestArchiveEntry("conflict.txt"));
        
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        ChangeSetResults results = performer.perform(in, out);
        
        assertTrue("Original should be deleted", results.getDeleted().contains("conflict.txt"));
        assertTrue("New version should be added", results.getAddedFromChangeSet().contains("conflict.txt"));
        assertEquals("Stream should have 0 entries (original deleted)", 0, results.getAddedFromStream().size());
    }
    
    // ---- Partition D: Exception & Defensive Guard Paths ----
    
    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorNullChangeSet() {
        new ChangeSetPerformer(null);
    }
    
    @Test(timeout = 4000)
    public void testPerformWithNullInputStream() throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        TestArchiveOutputStream out = new TestArchiveOutputStream(bout);
        ChangeSet changeSet = new ChangeSet();
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        
        try {
            performer.perform(null, out);
            fail("Should throw NullPointerException for null input stream");
        } catch (NullPointerException e) {
            // Expected
        }
    }
    
    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testPerformWithNullOutputStream() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[0]);
        TestArchiveInputStream in = new TestArchiveInputStream(bin);
        ChangeSet changeSet = new ChangeSet();
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        performer.perform(in, null);
    }
    
    // ---- Partition E: Object Lifecycle & Contract Integrity ----
    
    @Test(timeout = 4000)
    public void testChangeSetResultsContract() {
        ChangeSetResults results = new ChangeSetResults();
        
        assertNotNull("Results object should not be null", results);
        assertTrue("Added from stream should be empty", results.getAddedFromStream().isEmpty());
        assertTrue("Added from changeset should be empty", results.getAddedFromChangeSet().isEmpty());
        assertTrue("Deleted should be empty", results.getDeleted().isEmpty());
        
        results.addedFromStream("test.txt");
        results.addedFromChangeSet("new.txt");
        results.deleted("old.txt");
        
        assertEquals("Added from stream should have 1 entry", 1, results.getAddedFromStream().size());
        assertTrue("test.txt should be in added from stream", results.getAddedFromStream().contains("test.txt"));
        assertTrue("hasBeenAdded should return true for test.txt", results.hasBeenAdded("test.txt"));
        assertTrue("hasBeenAdded should return true for new.txt", results.hasBeenAdded("new.txt"));
        assertFalse("hasBeenAdded should return false for unknown.txt", results.hasBeenAdded("unknown.txt"));
    }
    
    @Test(timeout = 4000)
    public void testPerformMultipleCalls() throws IOException {
        // Test that performer can be called multiple times
        ChangeSet changeSet = new ChangeSet();
        changeSet.delete("file1.txt");
        
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);
        
        // First call
        ByteArrayInputStream bin1 = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream bout1 = new ByteArrayOutputStream();
        TestArchiveInputStream in1 = new TestArchiveInputStream(bin1);
        TestArchiveOutputStream out1 = new TestArchiveOutputStream(bout1);
        in1.addEntry(new TestArchiveEntry("file1.txt"));
        in1.addEntry(new TestArchiveEntry("file2.txt"));
        
        ChangeSetResults results1 = performer.perform(in1, out1);
        assertEquals("file1.txt should be deleted", 1, results1.getDeleted().size());
        assertEquals("file2.txt should remain", 1, results1.getAddedFromStream().size());
        
        // Second call with different data
        ByteArrayInputStream bin2 = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream bout2 = new ByteArrayOutputStream();
        TestArchiveInputStream in2 = new TestArchiveInputStream(bin2);
        TestArchiveOutputStream out2 = new TestArchiveOutputStream(bout2);
        in2.addEntry(new TestArchiveEntry("file1.txt"));
        in2.addEntry(new TestArchiveEntry("file3.txt"));
        
        ChangeSetResults results2 = performer.perform(in2, out2);
        assertEquals("file1.txt should still be deleted", 1, results2.getDeleted().size());
        assertEquals("file3.txt should remain", 1, results2.getAddedFromStream().size());
    }
    
    // ---- Helper Classes ----
    
    private static class TestArchiveEntry implements ArchiveEntry {
        private final String name;
        
        TestArchiveEntry(String name) {
            this.name = name;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public long getSize() {
            return 0;
        }
        
        @Override
        public boolean isDirectory() {
            return name != null && name.endsWith("/");
        }
        
        @Override
        public long getLastModified() {
            return System.currentTimeMillis();
        }
    }
    
    private static class TestArchiveInputStream extends ArchiveInputStream {
        private final ByteArrayInputStream bin;
        private final Set<ArchiveEntry> entries = new LinkedHashSet<ArchiveEntry>();
        private ArchiveEntry currentEntry;
        private boolean closed;
        
        TestArchiveInputStream(ByteArrayInputStream bin) {
            this.bin = bin;
        }
        
        void addEntry(ArchiveEntry entry) {
            entries.add(entry);
        }
        
        @Override
        public ArchiveEntry getNextEntry() throws IOException {
            if (entries.isEmpty()) {
                return null;
            }
            Iterator<ArchiveEntry> it = entries.iterator();
            currentEntry = it.next();
            it.remove();
            return currentEntry;
        }
        
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (closed) throw new IOException("Stream closed");
            if (currentEntry == null) return -1;
            return bin.read(b, off, len);
        }
        
        @Override
        public void close() throws IOException {
            closed = true;
            bin.close();
        }
    }
    
    private static class TestArchiveOutputStream extends ArchiveOutputStream {
        private final ByteArrayOutputStream bout;
        private ArchiveEntry currentEntry;
        private boolean closed;
        private boolean entryOpen;
        
        TestArchiveOutputStream(ByteArrayOutputStream bout) {
            this.bout = bout;
        }
        
        @Override
        public void putArchiveEntry(ArchiveEntry entry) throws IOException {
            if (closed) throw new IOException("Stream closed");
            currentEntry = entry;
            entryOpen = true;
        }
        
        @Override
        public void closeArchiveEntry() throws IOException {
            if (!entryOpen) throw new IOException("No entry open");
            currentEntry = null;
            entryOpen = false;
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (closed) throw new IOException("Stream closed");
            if (!entryOpen) throw new IOException("No open entry");
            bout.write(b, off, len);
        }
        
        @Override
        public void close() throws IOException {
            closed = true;
            bout.close();
        }
        
        @Override
        public void finish() throws IOException {
            // No-op for testing
        }
    }
}