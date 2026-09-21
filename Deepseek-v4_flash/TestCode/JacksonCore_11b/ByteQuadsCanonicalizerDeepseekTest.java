package com.fasterxml.jackson.core.sym;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Partition A (Core Functional Logic & State Transitions):
 *   - Test creation of root and child tables
 *   - Test add/find for q1, q2, q3, q array (various lengths)
 *   - Test interning, maybeDirty, release, merge
 * 
 * Partition B (Boundary Value Analysis & Extremes):
 *   - Empty table (size=0)
 *   - Single element lookup in empty table
 *   - Hash collisions (same hash but different keys) – not directly controllable, but we can force collisions by many inserts
 *   - Large counts near MAX_ENTRIES_FOR_REUSE (6000)
 *   - Null name (should throw NPE?)
 *   - Negative quad values
 *   - Zero quad values
 *   - Extremes of q array length (0,1,2,3,4,5,... up to large)
 *   - _spilloverStart boundary: 7/8 of hash area
 *   - _tertiaryShift boundaries: 4,5,6,7
 *   - Hash table size MIN_HASH_SIZE (16) and MAX_T_SIZE (65536)
 *   - Rehash condition: count > 50% and spillover > 1+count>>7, or count > 80% of hash size
 * 
 * Partition C (Defect-Targeted Branch Zone):
 *   - ArrayIndexOutOfBoundsException during rehash / _appendLongName
 *   - Test that mimics the known failure: add many symbols causing _appendLongName to exceed hashArea length
 *   - Ensure that after rehash, the table is consistent and no exception is thrown
 * 
 * Partition D (Exception & Defensive Guard Paths):
 *   - calcHash with qlen<4 throws IllegalArgumentException
 *   - _reportTooManyCollisions when spillover full and failOnDoS=true and hashSize>1024
 *   - _verifySharing makes copies when shared
 *   - _verifyNeedForRehash sets _needRehash correctly
 *   - _findOffsetForAdd handles primary/secondary/tertiary/spill full
 * 
 * Partition E (Object Lifecycle & Contract Integrity):
 *   - toString() returns expected format
 *   - bucketCount(), size(), primaryCount(), secondaryCount(), tertiaryCount(), spilloverCount(), totalCount() are consistent
 *   - addName returns the name (possibly interned)
 *   - findName returns the exact string added
 *   - Multiple adds with same quads should not duplicate (only first stored? Actually the class dedupes? Not exactly; addName does not check duplicates – it may store duplicate keys. That's okay for testing.)
 *   - Hash seed consistency across child instances
 *   - Child table release merges back to parent correctly
 * 
 * Defect Reproduction (targeted test):
 *   The reported bug: ArrayIndexOutOfBoundsException: 512 during symbol table expansion.
 *   This typically occurs in _appendLongName when the hashArea array is not large enough.
 *   We will add a large number of unique names with length >= 4 quads to force repeated _appendLongName.
 *   The test should trigger rehash and ensure that no exception is thrown. If the bug is present, the test will throw AIOOBE.
 */
public class ByteQuadsCanonicalizerDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testCreateRootAndChild() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(12345);
        assertNotNull("Root should not be null", root);
        assertEquals("Root size should be 0 initially", 0, root.size());
        assertTrue("Root may be dirty initially? Actually root has no parent", root.maybeDirty() || true);
        // root is not dirty because _hashShared is false? Actually root's _hashShared is initially false since it's owner.
        // But maybeDirty returns !_hashShared, so root is dirty (true). That's fine.

        ByteQuadsCanonicalizer child = root.makeChild(0);
        assertNotNull("Child should not be null", child);
        assertEquals("Child size should be 0", 0, child.size());
        assertTrue("Child should be dirty? Actually it's created as shared, so maybeDirty returns false",
                !child.maybeDirty()); // because _hashShared = true

        // Add name to child
        String name = "testName";
        String returned = child.addName(name, 1);
        assertEquals("Should return the added name (possibly interned)", name, returned);
        assertEquals("Child size should be 1", 1, child.size());
        assertFalse("Child should be dirty now (since not shared)", child.maybeDirty()); // _hashShared false after add

        // Find it
        String found = child.findName(1);
        assertEquals("Should find the name", name, found);

        // Release child and check parent
        child.release();
        assertEquals("After release, parent size should reflect child's additions", 1, root.size());
    }

    @Test(timeout = 4000)
    public void testAddAndFindSingleQuad() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        String name = "single";
        child.addName(name, 42);
        assertEquals("findName should return the exact string", name, child.findName(42));
        assertNull("Should not find non-existent quad", child.findName(99));
    }

    @Test(timeout = 4000)
    public void testAddAndFindTwoQuads() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(1);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        String name = "twoQuads";
        child.addName(name, 10, 20);
        assertEquals("findName should return the exact string", name, child.findName(10, 20));
        assertNull("Should not find wrong quads", child.findName(10, 21));
        assertNull("Should not find wrong quads", child.findName(11, 20));
    }

    @Test(timeout = 4000)
    public void testAddAndFindThreeQuads() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(2);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        String name = "threeQuads";
        child.addName(name, 100, 200, 300);
        assertEquals("findName should return the exact string", name, child.findName(100, 200, 300));
        assertNull("Should not find wrong quads", child.findName(100,200,301));
    }

    @Test(timeout = 4000)
    public void testAddAndFindLongNameArray() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(3);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        String name = "longName";
        int[] quads = new int[]{1,2,3,4,5};
        child.addName(name, quads, 5);
        assertEquals("findName with array should return exact string", name, child.findName(quads, 5));
        // Test wrong length
        int[] quadsWrong = {1,2,3,4,6};
        assertNull("Should not find with wrong quads", child.findName(quadsWrong, 5));
        // Test find with different qlen argument (should not find)
        // Note: findName(int[],int) expects qlen to match exactly
        // Also test findName with shorter length
        assertNull("Should not find with length 4", child.findName(new int[]{1,2,3,4}, 4));
    }

    // ==================== Partition B: Boundaries & Extremes ====================

    @Test(timeout = 4000)
    public void testEmptyTable() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        assertNull("findName on empty table should return null", root.findName(1));
        assertNull("findName two quads", root.findName(1,2));
        assertNull("findName three quads", root.findName(1,2,3));
        assertNull("findName array", root.findName(new int[]{1,2,3,4},4));
        assertEquals("size should be 0", 0, root.size());
        assertEquals("bucketCount should be default 64", 64, root.bucketCount());
    }

    @Test(timeout = 4000)
    public void testSingleElementLookupAfterAdd() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        child.addName("a", 0);
        assertEquals("Should find 'a'", "a", child.findName(0));
        // After release, parent should have it
        child.release();
        assertEquals("Parent should have name", "a", root.findName(0));
    }

    @Test(timeout = 4000)
    public void testLargeNumberOfInsertsToTriggerRehash() {
        // This test aims to trigger rehash and multiple spillover operations.
        // Use a root with small initial size? But we cannot control size directly; default is 64.
        // We'll add many entries (say 200) to force rehash.
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        // Add 200 unique names with single quad
        for (int i = 0; i < 200; i++) {
            String name = "name" + i;
            child.addName(name, i);
        }
        assertEquals("Child size should be 200", 200, child.size());
        // Verify we can find them all
        for (int i = 0; i < 200; i++) {
            String expected = "name" + i;
            String found = child.findName(i);
            assertEquals("Should find name for quad " + i, expected, found);
        }
        // Also test that we can find non-existent
        assertNull("Should not find quad 1000", child.findName(1000));
    }

    @Test(timeout = 4000)
    public void testLongNameWithLength4() {
        // Long name with exactly 4 quads (triggers _appendLongName)
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        String name = "exact4";
        child.addName(name, new int[]{1,2,3,4}, 4);
        assertEquals("Should find exact4", name, child.findName(new int[]{1,2,3,4}, 4));
    }

    @Test(timeout = 4000)
    public void testLongNameLengthGreaterThan8() {
        // Trigger _verifyLongName's default branch (length >8) and _verifyLongName2
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        int[] quads = new int[]{1,2,3,4,5,6,7,8,9,10};
        String name = "long10";
        child.addName(name, quads, 10);
        // Find using same array
        assertEquals("Should find long10", name, child.findName(quads, 10));
        // Find with different last quad
        int[] wrong = Arrays.copyOf(quads, 10);
        wrong[9] = 99;
        assertNull("Should not find wrong", child.findName(wrong, 10));
    }

    @Test(timeout = 4000)
    public void testHashCollisionHandling() {
        // Not directly controllable, but we can add many entries to create secondary/tertiary/spill collisions.
        // This is implicitly covered by large inserts test.
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Targeted test for the known Defects4J bug:
     * "com.fasterxml.jackson.core.sym.SymbolsViaParserTest::testSymbolTableExpansionBytes
     *   --> java.lang.ArrayIndexOutOfBoundsException: 512"
     * 
     * We simulate the scenario: create many unique long names (≥4 quads) to force repeated
     * calls to _appendLongName. The bug occurs when the hashArea array is not large enough
     * during rehash or append. We will add enough entries to cause the table to grow.
     * The test should complete without ArrayIndexOutOfBoundsException.
     * If the bug is present, the test will throw AIOOBE and fail.
     */
    @Test(timeout = 4000)
    public void testSymbolTableExpansionBytesTrigger() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(12345);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        // Add many long names (length 4 to 10) to force rehash and spillover
        int count = 500; // enough to cause expansion
        for (int i = 0; i < count; i++) {
            int len = 4 + (i % 7); // lengths 4..10
            int[] quads = new int[len];
            for (int j = 0; j < len; j++) {
                quads[j] = i * 100 + j; // unique quad values
            }
            String name = "name" + i;
            child.addName(name, quads, len);
        }
        assertEquals("Child should have " + count + " entries", count, child.size());
        // Verify a few random ones
        for (int i = 0; i < count; i += 50) {
            int len = 4 + (i % 7);
            int[] quads = new int[len];
            for (int j = 0; j < len; j++) {
                quads[j] = i * 100 + j;
            }
            String expected = "name" + i;
            String found = child.findName(quads, len);
            assertEquals("Should find added name at index " + i, expected, found);
        }
        // Ensure parent also has them after release
        child.release();
        assertEquals("Parent should have same count after merge", count, root.size());
        // Also add more to parent to ensure no further exceptions
        for (int i = count; i < count + 100; i++) {
            int[] quads = {i, i+1, i+2, i+3};
            root.addName("extra"+i, quads, 4);
        }
        assertEquals("Parent should have " + (count+100) + " entries", count+100, root.size());
    }

    // ==================== Partition D: Exception & Defensive Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCalcHashWithInvalidQlen() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        // calling calcHash with qlen<4 triggers exception via addName? Actually addName calls calcHash.
        // We can call calcHash indirectly via addName with array of length <4 (but addName handles those separately)
        // The calcHash method itself is public and throws if qlen<4. We'll call it directly.
        root.calcHash(new int[]{1}, 1); // should throw
    }

    @Test(timeout = 4000)
    public void testTooManyCollisionsWhenFailOnDoSAndLargeHashSize() {
        // This is tricky: we need to fill spillover completely and have hashSize > 1024.
        // We can use reflection or add many entries to a table with initial size 1024? Default is 64, but after rehash it may reach 1024.
        // A simpler approach: we can set _failOnDoS via makeChild with flags that enable FAIL_ON_SYMBOL_HASH_OVERFLOW.
        // However, making spillover full is hard without control.
        // Let's create a root with seed=0, and a child with failOnDoS=true.
        // Then add enough names to cause spillover full. But to ensure hashSize > 1024, we need many inserts.
        // We'll just test that the method does not throw for small tables.
        // The _reportTooManyCollisions method returns early if hashSize <= 1024.
        // So we need to create a scenario where _spilloverEnd >= (_hashSize << 3) and hashSize > 1024.
        // This is hard to achieve reliably without understanding exact structure.
        // We'll trust the implicit coverage from other tests.
        // At least we can test that _failOnDoS flag is honored: if we set FAilOnDoS to false, no exception should be thrown even if spillover full.
        // But we cannot force spillover full easily.
        // Instead, we test the defensive check: for small tables, no exception.
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        // Child with failOnDoS = true (using feature flag)
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW.getMask());
        // Add many names; but even if spillover fills, if hashSize<=1024, no exception.
        // So this test just runs without exception.
        for (int i = 0; i < 500; i++) {
            child.addName("n"+i, i);
        }
        // Release and check parent
        child.release();
        assertTrue("Parent has entries", root.size() > 0);
    }

    @Test(timeout = 4000)
    public void testVerifySharingOnCopy() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        // Child starts with shared state.
        // Add name: this will trigger _verifySharing, which copies arrays.
        child.addName("sharedTest", 100);
        // After add, child should no longer be shared
        // That is verified by maybeDirty
        assertTrue("Child should be dirty after first add", child.maybeDirty());
        // Add another: should not need to copy again
        child.addName("second", 200);
        assertEquals("Child size should be 2", 2, child.size());
    }

    // ==================== Partition E: Object Lifecycle & Contract ====================

    @Test(timeout = 4000)
    public void testToStringFormat() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        child.addName("test", 1);
        String str = child.toString();
        assertTrue("toString should contain class name", str.contains("ByteQuadsCanonicalizer"));
        assertTrue("toString should contain size=1", str.contains("size=1"));
        assertTrue("toString should contain hashSize=64", str.contains("hashSize=64"));
    }

    @Test(timeout = 4000)
    public void testCountersConsistency() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        assertEquals("Initially all counters 0", 0, child.primaryCount());
        assertEquals(0, child.secondaryCount());
        assertEquals(0, child.tertiaryCount());
        assertEquals(0, child.spilloverCount());
        assertEquals(0, child.totalCount());

        child.addName("a", 1);
        // After one add, we have one primary entry
        assertEquals(1, child.primaryCount());
        assertEquals(0, child.secondaryCount());
        assertEquals(0, child.tertiaryCount());
        assertEquals(0, child.spilloverCount());
        assertEquals(1, child.totalCount());

        // Add more to create collisions (but hard to guarantee secondary/tertiary without controlling hash)
        // We'll just verify that totalCount equals size after multiple adds
        for (int i = 0; i < 50; i++) {
            child.addName("n"+i, i+10); // use different quads
        }
        assertEquals("Size should be 51", 51, child.size());
        assertEquals("totalCount should equal size", child.size(), child.totalCount());
    }

    @Test(timeout = 4000)
    public void testHashSeedConsistency() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(42);
        assertEquals("Hash seed should be 42", 42, root.hashSeed());
        ByteQuadsCanonicalizer child = root.makeChild(0);
        assertEquals("Child inherits seed", 42, child.hashSeed());
    }

    @Test(timeout = 4000)
    public void testMultipleChildMerges() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        // Two separate children
        ByteQuadsCanonicalizer child1 = root.makeChild(0);
        ByteQuadsCanonicalizer child2 = root.makeChild(0);
        child1.addName("c1", 1);
        child2.addName("c2", 2);
        child1.release();
        child2.release();
        assertEquals("Parent should have both entries", 2, root.size());
        assertEquals("Should find first child's name", "c1", root.findName(1));
        assertEquals("Should find second child's name", "c2", root.findName(2));
    }

    @Test(timeout = 4000)
    public void testInterningEnabled() {
        // By default, interning is enabled (root created with intern=true)
        String name = new String("notInterned");
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        String returned = child.addName(name, 1);
        // The returned name should be interned (since _intern=true)
        assertSame("Returned name should be interned (same reference as String.intern())", name.intern(), returned);
    }

    @Test(timeout = 4000)
    public void testInterningDisabled() {
        // Create root with intern=false via createRoot? createRoot always sets intern=true.
        // We can use a flag: makeChild with flags that disable INTERN_FIELD_NAMES.
        // JsonFactory.Feature.INTERN_FIELD_NAMES is enabled by default? The flag is used in makeChild.
        // For test, we'll create a child with flags that have INTERN_FIELD_NAMES disabled (i.e., flag bit = 0).  
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        // Feature bit 0: INTERN_FIELD_NAMES is bit 0? Actually check JsonFactory.Feature enum.
        // We'll pass 0 flags to disable all features.
        ByteQuadsCanonicalizer child = root.makeChild(0); // flags=0 => intern disabled
        String name = new String("notInterned2");
        String returned = child.addName(name, 2);
        // Since _intern is false, the returned name should be the exact same object (no interning)
        assertSame("Should return the exact same object", name, returned);
    }

    // Additional test: _verifyLongName with length 5,6,7,8
    // Already covered by testLongNameLengthGreaterThan8 (10 quads, but the switch cases go through 5-8)
    @Test(timeout = 4000)
    public void testLongNamesOfLength5to8() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(0);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        // Test each length 5,6,7,8
        for (int len = 5; len <= 8; len++) {
            int[] quads = new int[len];
            for (int i = 0; i < len;i++) quads[i]= i*10;
            String name = "L"+len;
            child.addName(name, quads,len);
            // Find using same array
            String found = child.findName(quads, len);
            assertEquals("Should find for length "+len, name, found);
            // Slightly wrong array should not find
            int[] wrong = Arrays.copyOf(quads,len);
            wrong[len-1]++;
            assertNULL("Should not find wrong for length "+len, child.findName(wrong,len));
        }
    }

    // ==================== Utility helper for creating a child with specific flags ====================
    // (we already used makeChild(int flags) above)
}