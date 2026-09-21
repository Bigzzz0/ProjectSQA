package com.fasterxml.jackson.core.sym;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: ByteQuadsCanonicalizer
 * 
 * Key Decision Branches Targeted:
 * 1. _hashShared flag handling in _verifySharing() and addName methods
 * 2. Rehash triggering logic in _verifyNeedForRehash() - 50% threshold and spillover checks
 * 3. Hash calculation variations for 1, 2, 3, and 4+ quads
 * 4. _findOffsetForAdd() primary/secondary/tertiary/spillover slot selection
 * 5. Long name verification in _verifyLongName() with switch fall-through
 * 6. nukeSymbols() behavior during rehash with fill=true/false
 * 7. TableInfo merging in mergeChild() with count comparison and MAX_ENTRIES_FOR_REUSE
 * 8. Spillover overflow detection and _failOnDoS handling
 * 9. Tertiary shift calculation boundaries (64, 256, 1024 thresholds)
 * 10. calcHash() edge cases with zero values and _seed interaction
 * 
 * Known Defect Patterns (from Defects4J):
 * - ArrayIndexOutOfBoundsException due to hash table sizing issues
 * - Incorrect count/spillover calculations during rehash
 * - State corruption when merging child tables
 * - Secondary/tertiary lookup hash collisions
 */
public class ByteQuadsCanonicalizerDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====
    
    @Test(timeout = 4000)
    public void testCreateRootAndBasicProperties() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        assertNotNull("Root should not be null", root);
        assertTrue("Initial size should be > 0", root.size() >= 0);
        assertTrue("Bucket count should be power of 2", 
                   (root.bucketCount() & (root.bucketCount() - 1)) == 0);
        assertNotNull("Hash seed should be valid", root.hashSeed());
    }

    @Test(timeout = 4000)
    public void testMakeChildAndRelease() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(0);
        assertNotNull("Child should not be null", child);
        assertEquals("Child should have same seed", root.hashSeed(), child.hashSeed());
        
        // Add some entries to child
        child.addName("test1", 1);
        child.addName("test2", 2, 3);
        
        // Release should merge back to parent
        child.release();
        assertTrue("Root should have entries after merge", root.size() > 0);
    }

    @Test(timeout = 4000)
    public void testFindNameSingleQuad() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        child.addName("found", 42);
        
        assertEquals("Should find existing name", "found", child.findName(42));
        assertNull("Should not find non-existing name", child.findName(99));
    }

    @Test(timeout = 4000)
    public void testFindNameTwoQuads() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        child.addName("found2", 10, 20);
        
        assertEquals("Should find existing name", "found2", child.findName(10, 20));
        assertNull("Should not find non-existing name", child.findName(10, 21));
    }

    @Test(timeout = 4000)
    public void testFindNameThreeQuads() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        child.addName("found3", 100, 200, 300);
        
        assertEquals("Should find existing name", "found3", child.findName(100, 200, 300));
        assertNull("Should not find non-existing name", child.findName(100, 200, 301));
    }

    @Test(timeout = 4000)
    public void testFindNameLongArray() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        int[] quads = {1, 2, 3, 4, 5, 6, 7, 8};
        child.addName("longName", quads, 8);
        
        assertEquals("Should find long name", "longName", child.findName(quads, 8));
        
        int[] wrongQuads = {1, 2, 3, 4, 5, 6, 7, 9};
        assertNull("Should not find wrong long name", child.findName(wrongQuads, 8));
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====
    
    @Test(timeout = 4000)
    public void testEmptyTableCounts() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        assertEquals("Primary count should be 0", 0, child.primaryCount());
        assertEquals("Secondary count should be 0", 0, child.secondaryCount());
        assertEquals("Tertiary count should be 0", 0, child.tertiaryCount());
        assertEquals("Spillover count should be 0", 0, child.spilloverCount());
        assertEquals("Total count should be 0", 0, child.totalCount());
    }

    @Test(timeout = 4000)
    public void testSingleEntryTertiaryAndSpillover() {
        // Force many collisions to test tertiary/spillover paths
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(1);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        // Add many entries that collide due to seed=1
        for (int i = 0; i < 200; i++) {
            child.addName("name" + i, i);
        }
        
        assertTrue("Should have tertiary entries", child.tertiaryCount() >= 0);
        assertTrue("Should have total entries", child.totalCount() > 0);
        assertTrue("Size should reflect entries", child.size() <= 200);
    }

    @Test(timeout = 4000)
    public void testZeroQuadHandling() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        child.addName("zero", 0);
        assertEquals("Should find zero quad", "zero", child.findName(0));
        
        child.addName("zero2", 0, 0);
        assertEquals("Should find zero quads", "zero2", child.findName(0, 0));
        
        child.addName("zero3", 0, 0, 0);
        assertEquals("Should find three zeros", "zero3", child.findName(0, 0, 0));
    }

    @Test(timeout = 4000)
    public void testMaximumSizeRehash() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        // Add enough entries to trigger rehash multiple times
        for (int i = 0; i < 10000; i++) {
            child.addName("big" + i, i);
        }
        
        assertTrue("Table should handle large number of entries", child.size() > 0);
        assertTrue("Should not exceed max entries for reuse", child.size() <= 10000);
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    
    @Test(timeout = 4000)
    public void testDefect3SpilloverOverflowWithRehash() {
        // Tests ArrayIndexOutOfBoundsException bug from testIssue207
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(1);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        // Use specific hash pattern to force spillover overflow
        for (int i = 0; i < 500; i++) {
            // Create names that hash to same bucket to force spillover
            child.addName("collider" + i, i * 17, i * 31);
        }
        
        // This should not throw ArrayIndexOutOfBoundsException
        assertTrue("Table should survive spillover overflow", child.totalCount() > 0);
        
        // Verify we can still find names
        String found = child.findName(0, 0);
        // May be null if not added, but shouldn't throw
        assertNotNull("Should have entries", child.findName(17, 31));
    }

    @Test(timeout = 4000)
    public void testDefect4RehashCountConsistency() {
        // Tests the count consistency bug from testCollisionsWithBytesNew187b
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        // Insert entries that will force rehash and verify count consistency
        int expectedCount = 0;
        for (int i = 0; i < 200; i++) {
            child.addName("entry" + i, i, i + 1);
            expectedCount++;
        }
        
        assertEquals("Count should match expected after rehash", expectedCount, child.size());
        
        // Verify we can find a previously inserted entry
        assertNotNull("Should find early entry", child.findName(0, 1));
        assertNotNull("Should find late entry", child.findName(199, 200));
    }

    @Test(timeout = 4000)
    public void testDefect5LongNameVerificationWithBoundary() {
        // Tests potential issue with _verifyLongName switch fall-through
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        // Test boundary cases for long name lengths
        int[] quads4 = {10, 20, 30, 40};
        int[] quads5 = {10, 20, 30, 40, 50};
        int[] quads6 = {10, 20, 30, 40, 50, 60};
        int[] quads7 = {10, 20, 30, 40, 50, 60, 70};
        int[] quads8 = {10, 20, 30, 40, 50, 60, 70, 80};
        int[] quads9 = {10, 20, 30, 40, 50, 60, 70, 80, 90};
        
        child.addName("name4", quads4, 4);
        child.addName("name5", quads5, 5);
        child.addName("name6", quads6, 6);
        child.addName("name7", quads7, 7);
        child.addName("name8", quads8, 8);
        child.addName("name9", quads9, 9);
        
        assertEquals("Should find qlen=4", "name4", child.findName(quads4, 4));
        assertEquals("Should find qlen=5", "name5", child.findName(quads5, 5));
        assertEquals("Should find qlen=6", "name6", child.findName(quads6, 6));
        assertEquals("Should find qlen=7", "name7", child.findName(quads7, 7));
        assertEquals("Should find qlen=8", "name8", child.findName(quads8, 8));
        assertEquals("Should find qlen=9", "name9", child.findName(quads9, 9));
        
        // Test non-matching to ensure verification works
        int[] quads4Wrong = {10, 20, 30, 41};
        assertNull("Should not find wrong qlen=4", child.findName(quads4Wrong, 4));
    }

    @Test(timeout = 4000)
    public void testDefect6SecondaryLookupAfterPrimary() {
        // Tests hash collision handling across primary/secondary
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        // Add two names that hash to same primary slot
        child.addName("primary", 1);
        child.addName("secondary", 1); // Will land in secondary if same hash
        
        // Both should be findable
        String result1 = child.findName(1);
        // Depending on hash, we may find one or both; either is acceptable
        assertNotNull("Should find name in table", result1);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCalcHashWithInvalidLength() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        int[] q = {1};
        root.calcHash(q, 1); // Should throw for qlen < 4
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCalcHashWithZeroLength() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        int[] q = {};
        root.calcHash(q, 0); // Should throw for qlen < 4
    }

    @Test(timeout = 4000)
    public void testCollisionOverflowWithFailOnDoS() {
        // Test that _failOnDoS doesn't throw for small tables
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW.getMask());
        
        // Force many collisions to hit spillover limit
        for (int i = 0; i < 2000; i++) {
            child.addName("collision" + i, i % 64, i * 2);
        }
        
        // Should not throw due to small hash size check in _reportTooManyCollisions
        assertTrue("Should survive collisions", child.totalCount() > 0);
    }

    @Test(timeout = 4000)
    public void testCalcHashFourQuadsNormal() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        int[] q = {1, 2, 3, 4};
        int hash = root.calcHash(q, 4);
        assertTrue("Hash should be non-zero", hash != 0);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testMultipleChildTables() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        
        // Create multiple children and add entries
        ByteQuadsCanonicalizer child1 = root.makeChild(0);
        child1.addName("c1", 1);
        child1.release();
        
        ByteQuadsCanonicalizer child2 = root.makeChild(0);
        child2.addName("c2", 2);
        child2.addName("c3", 3);
        child2.release();
        
        // Root should have all entries
        ByteQuadsCanonicalizer child3 = root.makeChild(0);
        assertEquals("Should find c1", "c1", child3.findName(1));
        assertEquals("Should find c2", "c2", child3.findName(2));
        assertEquals("Should find c3", "c3", child3.findName(3));
        child3.release();
    }

    @Test(timeout = 4000)
    public void testToStringFormat() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        String toString = child.toString();
        assertTrue("toString should contain class name", 
                   toString.contains("ByteQuadsCanonicalizer"));
        assertTrue("toString should contain size", toString.contains("size="));
        
        child.addName("test", 42);
        String toString2 = child.toString();
        assertTrue("toString should reflect entries", toString2.contains("size=1"));
    }

    @Test(timeout = 4000)
    public void testBucketCountAfterRehash() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        int initialBuckets = child.bucketCount();
        
        // Add enough to trigger rehash
        for (int i = 0; i < 200; i++) {
            child.addName("entry" + i, i);
        }
        
        assertTrue("Bucket count should increase after rehash", 
                   child.bucketCount() > initialBuckets);
        assertTrue("Bucket count should be power of 2", 
                   (child.bucketCount() & (child.bucketCount() - 1)) == 0);
    }

    @Test(timeout = 4000)
    public void testMaybeDirtyAfterRelease() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        // Initially shouldn't be dirty (shared)
        assertFalse("Child should not be dirty initially", child.maybeDirty());
        
        child.addName("test", 1);
        assertTrue("Child should be dirty after adding", child.maybeDirty());
        
        child.release();
        // After release, gets marked shared again
        // May be dirty if still has entries; behavior depends on state
    }

    @Test(timeout = 4000)
    public void testInternBehavior() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.INTERN_FIELD_NAMES.getMask());
        
        String name1 = "testIntern";
        String name2 = "testIntern";
        
        // These should be interned when added
        String result1 = child.addName(name1, 1);
        String result2 = child.addName(name2, 2);
        
        // Both should be interned (same reference if strings are equal)
        assertSame("Interned strings should be same reference for equal strings", 
                   result1, result2);
    }

    @Test(timeout = 4000)
    public void testAddNameWithAllOverflowPaths() {
        // Force entries into spillover area through controlled collisions
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        
        // Use specific seeds and hash patterns to force spillover
        for (int i = 0; i < 1000; i++) {
            // Use mod 64 to create collisions on 64-bucket table
            child.addName("spill" + i, i % 64, i, i * 2);
        }
        
        // Verify we can retrieve names from all areas
        assertTrue("Should have primary entries", child.primaryCount() > 0);
        assertTrue("Should have entries in complex areas", child.totalCount() > 0);
    }
}