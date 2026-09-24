package com.fasterxml.jackson.core.sym;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Defects4J Issue:
 * - SymbolsViaParserTest::testSymbolTableExpansionBytes -> java.lang.ArrayIndexOutOfBoundsException: 512
 *   Root cause: When a child table fills its spillover slots up to the hash area boundary (_spilloverEnd == 512)
 *   and is released, its TableInfo is saved to the root. A subsequent child table created from that state
 *   inherits _spilloverEnd == 512 with _needRehash == false. Adding another colliding symbol in child table
 *   fails to trigger rehash during _verifySharing(), causing _findOffsetForAdd() to return 512, which triggers
 *   an ArrayIndexOutOfBoundsException on _hashArea[512].
 *
 * Specific Decision Branches & Boundary Conditions Targeted:
 * 1. Cascade lookup & insertion across Primary, Secondary, Tertiary buckets, and Spillover area.
 * 2. Empty-slot short-circuiting in primary and secondary lookups (len == 0).
 * 3. _verifyLongName switch statements: unrolled cases (4, 5, 6, 7, 8) and loop fallback (_verifyLongName2 for len > 8),
 *    as well as verification failure paths when hashes collide but quad contents differ.
 * 4. Multi-quad delegation in findName/addName for qlen == 1, 2, 3 to specialized quad methods.
 * 5. Table resizing / rehash triggering:
 *    - Rehash on load threshold (> 50% with spillovers or > 80% capacity).
 *    - Child table state release and mergeChild logic:
 *      * Child count unchanged -> early return.
 *      * Child count > MAX_ENTRIES_FOR_REUSE (6000) -> table cleanup/reset.
 * 6. Minimum hash size clamping (MIN_HASH_SIZE = 16) and non-power-of-two padding in constructor.
 * 7. _calcTertiaryShift branch evaluation (<64 -> 4, <=256 -> 5, <=1024 -> 6, >1024 -> 7).
 * 8. _reportTooManyCollisions DoS protection:
 *    - Suppressed when _hashSize <= 1024.
 *    - Throws IllegalStateException when _hashSize > 1024.
 * 9. String interning toggle via JsonFactory.Feature.INTERN_FIELD_NAMES flag.
 */
public class ByteQuadsCanonicalizerGeminiTest {

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testCreateRootAndChildLifecycle() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(12345);
        assertEquals(0, root.size());
        assertEquals(64, root.bucketCount());
        assertEquals(12345, root.hashSeed());
        assertFalse(root.maybeDirty());

        // Test non-parameterized root creation (seed generated dynamically)
        ByteQuadsCanonicalizer dynamicRoot = ByteQuadsCanonicalizer.createRoot();
        assertNotNull(dynamicRoot);
        assertEquals(0, dynamicRoot.size());

        int flags = JsonFactory.Feature.collectDefaults();
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        assertNotNull(child);
        assertEquals(0, child.size());
        assertEquals(64, child.bucketCount());
        assertEquals(12345, child.hashSeed());
        assertFalse(child.maybeDirty());

        // Adding symbol marks child dirty
        child.addName("sym1", 100);
        assertEquals(1, child.size());
        assertTrue(child.maybeDirty());
        assertEquals("sym1", child.findName(100));

        // Parent unaffected before release
        assertEquals(0, root.size());

        child.release();
        // Child marked clean/shared post-release
        assertFalse(child.maybeDirty());
        // Parent received merged state
        assertEquals(1, root.size());
    }

    @Test(timeout = 4000)
    public void testBasic1QuadFindAndAdd() {
        ByteQuadsCanonicalizer table = ByteQuadsCanonicalizer.createRoot(111).makeChild(JsonFactory.Feature.collectDefaults());
        assertNull(table.findName(42));

        String added = table.addName("singleQuad", 42);
        assertEquals("singleQuad", added);
        assertEquals("singleQuad", table.findName(42));
        assertNull(table.findName(43));
    }

    @Test(timeout = 4000)
    public void testBasic2QuadFindAndAdd() {
        ByteQuadsCanonicalizer table = ByteQuadsCanonicalizer.createRoot(222).makeChild(JsonFactory.Feature.collectDefaults());
        assertNull(table.findName(10, 20));

        // Standard 2-quad
        table.addName("twoQuads", 10, 20);
        assertEquals("twoQuads", table.findName(10, 20));
        assertNull(table.findName(10, 21));
        assertNull(table.findName(11, 20));

        // Corner case: q2 == 0 uses calcHash(q1) internally
        table.addName("zeroSecondQuad", 99, 0);
        assertEquals("zeroSecondQuad", table.findName(99, 0));
    }

    @Test(timeout = 4000)
    public void testBasic3QuadFindAndAdd() {
        ByteQuadsCanonicalizer table = ByteQuadsCanonicalizer.createRoot(333).makeChild(JsonFactory.Feature.collectDefaults());
        assertNull(table.findName(1, 2, 3));

        table.addName("threeQuads", 1, 2, 3);
        assertEquals("threeQuads", table.findName(1, 2, 3));
        assertNull(table.findName(1, 2, 4));
        assertNull(table.findName(1, 5, 3));
        assertNull(table.findName(9, 2, 3));
    }

    @Test(timeout = 4000)
    public void testBasicMultiQuadFindAndAdd() {
        ByteQuadsCanonicalizer table = ByteQuadsCanonicalizer.createRoot(444).makeChild(JsonFactory.Feature.collectDefaults());
        int[] q4 = new int[] { 10, 20, 30, 40 };
        int[] q5 = new int[] { 10, 20, 30, 40, 50 };
        int[] q9 = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

        assertNull(table.findName(q4, 4));
        table.addName("quad4", q4, 4);
        assertEquals("quad4", table.findName(q4, 4));

        table.addName("quad5", q5, 5);
        assertEquals("quad5", table.findName(q5, 5));

        // Length > 8 exercises _verifyLongName2 loop branch
        table.addName("quad9", q9, 9);
        assertEquals("quad9", table.findName(q9, 9));

        assertNull(table.findName(new int[] { 10, 20, 30, 41 }, 4));
        assertNull(table.findName(new int[] { 10, 20, 30, 40, 51 }, 5));
        assertNull(table.findName(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 10 }, 9));
    }

    @Test(timeout = 4000)
    public void testInterningEnabledAndDisabled() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(777);

        // Child with interning ENABLED
        int flagsWithIntern = JsonFactory.Feature.INTERN_FIELD_NAMES.getMask();
        ByteQuadsCanonicalizer childWithIntern = root.makeChild(flagsWithIntern);
        String name1 = new String("distinctStringInterned");
        String added1 = childWithIntern.addName(name1, 101);
        assertSame(name1.intern(), added1);

        // Child with interning DISABLED
        int flagsNoIntern = 0;
        ByteQuadsCanonicalizer childNoIntern = root.makeChild(flagsNoIntern);
        String name2 = new String("distinctStringNotInterned");
        String added2 = childNoIntern.addName(name2, 102);
        assertSame(name2, added2);
    }

    @Test(timeout = 4000)
    public void testArrayDelegationForShortQuads() {
        ByteQuadsCanonicalizer table = ByteQuadsCanonicalizer.createRoot(555).makeChild(JsonFactory.Feature.collectDefaults());

        table.addName("arrLen1", new int[] { 11 }, 1);
        assertEquals("arrLen1", table.findName(11));
        assertEquals("arrLen1", table.findName(new int[] { 11 }, 1));

        table.addName("arrLen2", new int[] { 22, 33 }, 2);
        assertEquals("arrLen2", table.findName(22, 33));
        assertEquals("arrLen2", table.findName(new int[] { 22, 33 }, 2));

        table.addName("arrLen3", new int[] { 44, 55, 66 }, 3);
        assertEquals("arrLen3", table.findName(44, 55, 66));
        assertEquals("arrLen3", table.findName(new int[] { 44, 55, 66 }, 3));
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Collisions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testCollisionsCascadingPrimarySecondaryTertiarySpillover1Quad() {
        ByteQuadsCanonicalizer table = ByteQuadsCanonicalizer.createRoot(888).makeChild(JsonFactory.Feature.collectDefaults());
        int targetBucket = table.calcHash(1) & 63;
        List<Integer> colliders = new ArrayList<>();
        for (int i = 1; colliders.size() < 12; i++) {
            if ((table.calcHash(i) & 63) == targetBucket) {
                colliders.add(i);
            }
        }

        // 1st entry -> Primary
        table.addName("c0", colliders.get(0));
        assertEquals(1, table.primaryCount());

        // 2nd entry -> Secondary
        table.addName("c1", colliders.get(1));
        assertEquals(1, table.secondaryCount());

        // 3rd to 6th entries -> Tertiary (bucket size 4 slots)
        for (int i = 2; i <= 5; i++) {
            table.addName("c" + i, colliders.get(i));
        }
        assertEquals(4, table.tertiaryCount());

        // 7th+ entries -> Spillover
        table.addName("c6", colliders.get(6));
        table.addName("c7", colliders.get(7));
        assertEquals(2, table.spilloverCount());

        // Verify lookups across all tiers
        assertEquals("c0", table.findName(colliders.get(0)));
        assertEquals("c1", table.findName(colliders.get(1)));
        assertEquals("c2", table.findName(colliders.get(2)));
        assertEquals("c5", table.findName(colliders.get(5)));
        assertEquals("c6", table.findName(colliders.get(6)));
        assertEquals("c7", table.findName(colliders.get(7)));
        assertNull(table.findName(colliders.get(8)));
    }

    @Test(timeout = 4000)
    public void testCollisionsCascading2Quad() {
        ByteQuadsCanonicalizer table = ByteQuadsCanonicalizer.createRoot(999).makeChild(JsonFactory.Feature.collectDefaults());
        int targetBucket = table.calcHash(1, 100) & 63;
        List<int[]> colliders = new ArrayList<>();
        for (int i = 1; colliders.size() < 8; i++) {
            if ((table.calcHash(i, i * 7) & 63) == targetBucket) {
                colliders.add(new int[] { i, i * 7 });
            }
        }

        for (int i = 0; i < colliders.size(); i++) {
            table.addName("2q_" + i, colliders.get(i)[0], colliders.get(i)[1]);
        }

        for (int i = 0; i < colliders.size(); i++) {
            assertEquals("2q_" + i, table.findName(colliders.get(i)[0], colliders.get(i)[1]));
        }
        assertNull(table.findName(colliders.get(0)[0], colliders.get(0)[1] + 99999));
    }

    @Test(timeout = 4000)
    public void testCollisionsCascading3Quad() {
        ByteQuadsCanonicalizer table = ByteQuadsCanonicalizer.createRoot(1010).makeChild(JsonFactory.Feature.collectDefaults());
        int targetBucket = table.calcHash(1, 2, 3) & 63;
        List<int[]> colliders = new ArrayList<>();
        for (int i = 1; colliders.size() < 8; i++) {
            if ((table.calcHash(i, i * 3, i * 5) & 63) == targetBucket) {
                colliders.add(new int[] { i, i * 3, i * 5 });
            }
        }

        for (int i = 0; i < colliders.size(); i++) {
            table.addName("3q_" + i, colliders.get(i)[0], colliders.get(i)[1], colliders.get(i)[2]);
        }

        for (int i = 0; i < colliders.size(); i++) {
            assertEquals("3q_" + i, table.findName(colliders.get(i)[0], colliders.get(i)[1], colliders.get(i)[2]));
        }
        assertNull(table.findName(colliders.get(0)[0], colliders.get(0)[1], colliders.get(0)[2] + 99999));
    }

    @Test(timeout = 4000)
    public void testCollisionsCascadingMultiQuad() {
        ByteQuadsCanonicalizer table = ByteQuadsCanonicalizer.createRoot(1212).makeChild(JsonFactory.Feature.collectDefaults());
        int targetBucket = table.calcHash(new int[] { 1, 2, 3, 4 }, 4) & 63;
        List<int[]> colliders = new ArrayList<>();
        for (int i = 1; colliders.size() < 8; i++) {
            int[] q = new int[] { i, i * 2, i * 3, i * 4 };
            if ((table.calcHash(q, 4) & 63) == targetBucket) {
                colliders.add(q);
            }
        }

        for (int i = 0; i < colliders.size(); i++) {
            table.addName("4q_" + i, colliders.get(i), 4);
        }

        for (int i = 0; i < colliders.size(); i++) {
            assertEquals("4q_" + i, table.findName(colliders.get(i), 4));
        }
        assertNull(table.findName(new int[] { colliders.get(0)[0], colliders.get(0)[1], colliders.get(0)[2], 999999 }, 4));
    }

    @Test(timeout = 4000)
    public void testLongNameVerificationMismatchCases() {
        ByteQuadsCanonicalizer table = ByteQuadsCanonicalizer.createRoot(1313).makeChild(JsonFactory.Feature.collectDefaults());

        // Quad pairs designed such that quad[0..2] are identical, and remaining quads produce
        // the exact same additive sum, yielding IDENTICAL calcHash results and triggering _verifyLongName checks
        int[] q4A = new int[] { 1, 2, 3, 10 };
        table.addName("len4", q4A, 4);

        int[] q5A = new int[] { 1, 2, 3, 10, 20 };
        int[] q5B = new int[] { 1, 2, 3, 20, 10 };
        assertEquals(table.calcHash(q5A, 5), table.calcHash(q5B, 5));
        table.addName("len5", q5A, 5);
        assertNull(table.findName(q5B, 5)); // Mismatch on case 5 -> case 4

        int[] q6A = new int[] { 1, 2, 3, 10, 20, 30 };
        int[] q6B = new int[] { 1, 2, 3, 20, 10, 30 };
        assertEquals(table.calcHash(q6A, 6), table.calcHash(q6B, 6));
        table.addName("len6", q6A, 6);
        assertNull(table.findName(q6B, 6)); // Mismatch on case 6

        int[] q7A = new int[] { 1, 2, 3, 10, 20, 30, 40 };
        int[] q7B = new int[] { 1, 2, 3, 20, 10, 30, 40 };
        assertEquals(table.calcHash(q7A, 7), table.calcHash(q7B, 7));
        table.addName("len7", q7A, 7);
        assertNull(table.findName(q7B, 7)); // Mismatch on case 7

        int[] q8A = new int[] { 1, 2, 3, 10, 20, 30, 40, 50 };
        int[] q8B = new int[] { 1, 2, 3, 20, 10, 30, 40, 50 };
        assertEquals(table.calcHash(q8A, 8), table.calcHash(q8B, 8));
        table.addName("len8", q8A, 8);
        assertNull(table.findName(q8B, 8)); // Mismatch on case 8

        int[] q9A = new int[] { 1, 2, 3, 10, 20, 30, 40, 50, 60 };
        int[] q9B = new int[] { 1, 2, 3, 20, 10, 30, 40, 50, 60 };
        assertEquals(table.calcHash(q9A, 9), table.calcHash(q9B, 9));
        table.addName("len9", q9A, 9);
        assertNull(table.findName(q9B, 9)); // Mismatch in _verifyLongName2
    }

    @Test(timeout = 4000)
    public void testCalcTertiaryShiftBranches() {
        assertEquals(4, ByteQuadsCanonicalizer._calcTertiaryShift(64));   // tertSlots = 16 (< 64)
        assertEquals(5, ByteQuadsCanonicalizer._calcTertiaryShift(512));  // tertSlots = 128 (<= 256)
        assertEquals(6, ByteQuadsCanonicalizer._calcTertiaryShift(2048)); // tertSlots = 512 (<= 1024)
        assertEquals(7, ByteQuadsCanonicalizer._calcTertiaryShift(8192)); // tertSlots = 2048 (> 1024)
    }

    @Test(timeout = 4000)
    public void testConstructorSizeRoundingAndPadding() throws Exception {
        Constructor<ByteQuadsCanonicalizer> ctor = ByteQuadsCanonicalizer.class.getDeclaredConstructor(
                int.class, boolean.class, int.class, boolean.class);
        ctor.setAccessible(true);

        // Clamping to MIN_HASH_SIZE (16)
        ByteQuadsCanonicalizer clamped = ctor.newInstance(8, true, 123, true);
        assertEquals(16, clamped.bucketCount());

        // Rounding up non-power-of-2 (20 -> 32)
        ByteQuadsCanonicalizer rounded = ctor.newInstance(20, true, 123, true);
        assertEquals(32, rounded.bucketCount());
    }

    @Test(timeout = 4000)
    public void testToStringAndCounters() {
        ByteQuadsCanonicalizer table = ByteQuadsCanonicalizer.createRoot(1414).makeChild(JsonFactory.Feature.collectDefaults());
        table.addName("a", 1);
        table.addName("b", 2);

        assertEquals(2, table.size());
        assertEquals(table.primaryCount() + table.secondaryCount() + table.tertiaryCount() + table.spilloverCount(),
                     table.totalCount());

        String repr = table.toString();
        assertNotNull(repr);
        assertTrue(repr.contains("ByteQuadsCanonicalizer"));
        assertTrue(repr.contains("size=2"));
        assertTrue(repr.contains("hashSize=64"));
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone
    /**********************************************************
     */

    /**
     * Dedicated defect-revealing test for Jackson-core issue:
     * SymbolsViaParserTest::testSymbolTableExpansionBytes -> ArrayIndexOutOfBoundsException: 512
     *
     * In the defective version, child1 completely exhausts spillover slots up to 512, setting _needRehash=true.
     * When released, TableInfo stores spilloverEnd=512.
     * When child2 is constructed from root, _needRehash is reset to false.
     * When child2 adds an entry that collides into spillover, child2 fails to rehash in _verifySharing()
     * and attempts to write at offset 512 of _hashArea (length 512), throwing ArrayIndexOutOfBoundsException.
     */
    @Test(timeout = 4000)
    public void testSymbolTableExpansionBytesDefect() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(12345);
        ByteQuadsCanonicalizer child1 = root.makeChild(JsonFactory.Feature.collectDefaults());

        int targetBucket = root.calcHash(1) & 63;
        List<Integer> colliders = new ArrayList<>();
        // Collect 23 values hashing to the same bucket
        // 1 primary + 1 secondary + 4 tertiary + 16 spillover slots = 22 to fill the table to the exact end
        for (int i = 1; colliders.size() < 24; i++) {
            if ((root.calcHash(i) & 63) == targetBucket) {
                colliders.add(i);
            }
        }

        // Fill primary (1), secondary (1), tertiary (4), and all 16 spillover slots
        for (int i = 0; i < 22; i++) {
            child1.addName("sym_" + i, colliders.get(i));
        }

        assertEquals(16, child1.spilloverCount());
        child1.release();

        // Create child2 inheriting the full spillover state
        ByteQuadsCanonicalizer child2 = root.makeChild(JsonFactory.Feature.collectDefaults());

        // This addition MUST NOT throw ArrayIndexOutOfBoundsException: 512
        String added = child2.addName("sym_22", colliders.get(22));
        assertNotNull(added);
        assertEquals("sym_22", child2.findName(colliders.get(22)));
    }

    @Test(timeout = 4000)
    public void testSymbolTableExpansionAcrossRehashes() {
        ByteQuadsCanonicalizer table = ByteQuadsCanonicalizer.createRoot(1515).makeChild(JsonFactory.Feature.collectDefaults());
        assertEquals(64, table.bucketCount());

        // Add 120 unique symbols to trigger multiple rehashes (64 -> 128 -> 256)
        for (int i = 0; i < 120; i++) {
            table.addName("rehash_" + i, i + 1000);
        }

        assertTrue("Table should have expanded past 64", table.bucketCount() > 64);
        assertEquals(120, table.size());

        // Verify all 120 symbols survived the rehashes
        for (int i = 0; i < 120; i++) {
            assertEquals("rehash_" + i, table.findName(i + 1000));
        }
    }

    @Test(timeout = 4000)
    public void testMaxEntriesForReuseResetOnChildRelease() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(1616);
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());

        // Exceed MAX_ENTRIES_FOR_REUSE (6000)
        for (int i = 0; i < 6005; i++) {
            child.addName("n" + i, i + 1);
        }
        assertEquals(6005, child.size());

        child.release();

        // Root table should be reset to empty default initial size
        assertEquals(0, root.size());
        assertEquals(64, root.bucketCount());
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCalcHashInvalidLengthThrows() {
        ByteQuadsCanonicalizer table = ByteQuadsCanonicalizer.createRoot(1717);
        table.calcHash(new int[] { 1, 2, 3 }, 3);
    }

    @Test(timeout = 4000)
    public void testReportTooManyCollisionsBranches() {
        ByteQuadsCanonicalizer table = ByteQuadsCanonicalizer.createRoot(1818).makeChild(JsonFactory.Feature.collectDefaults());

        // Branch 1: Suppressed when _hashSize <= 1024
        assertEquals(64, table.bucketCount());
        table._reportTooManyCollisions(); // Should not throw

        // Expand table so _hashSize > 1024
        for (int i = 0; i < 900; i++) {
            table.addName("exp_" + i, i + 1);
        }
        assertTrue(table.bucketCount() > 1024);

        // Branch 2: Throws IllegalStateException when full & > 1024
        try {
            table._reportTooManyCollisions();
            fail("Expected IllegalStateException for excessive collisions");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("suspect a DoS attack based on hash collisions"));
        }
    }

    @Test(timeout = 4000)
    public void testEmptySlotShortCircuitsInLookup() {
        ByteQuadsCanonicalizer table = ByteQuadsCanonicalizer.createRoot(1919).makeChild(JsonFactory.Feature.collectDefaults());

        // Insert a 2-quad entry
        table.addName("k1", 100, 200);

        // Search for 1-quad: primary slot has len=2 (!= 1), falls back to secondary slot.
        // Secondary slot is empty (len=0), hitting the short-circuit return null.
        assertNull(table.findName(100));

        // Search for 3-quad: primary len=2 (!= 3), secondary slot empty (len=0) -> returns null
        assertNull(table.findName(100, 200, 300));

        // Search for multi-quad (len=4): primary len=2 (!= 4), secondary len=0 -> returns null
        assertNull(table.findName(new int[] { 100, 200, 300, 400 }, 4));
    }

    /*
    /**********************************************************
    /* Partition E: Object Lifecycle & State Integrity
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testChildTableMaybeDirtyStateTransitions() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(2020);
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());

        assertFalse("Clean child must not be dirty", child.maybeDirty());

        child.addName("first", 1);
        assertTrue("Child must be dirty after addName", child.maybeDirty());

        child.release();
        assertFalse("Child must be marked clean after release", child.maybeDirty());
    }

    @Test(timeout = 4000)
    public void testReleaseWhenNotDirtyIsNoOp() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(2121);
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());

        // Release without additions
        child.release();
        assertEquals(0, root.size());
    }

    @Test(timeout = 4000)
    public void testReleaseWhenCountUnchangedDoesNotOverwrite() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(2222);
        ByteQuadsCanonicalizer child1 = root.makeChild(JsonFactory.Feature.collectDefaults());
        child1.addName("entry1", 1);
        child1.release();
        assertEquals(1, root.size());

        ByteQuadsCanonicalizer child2 = root.makeChild(JsonFactory.Feature.collectDefaults());
        // child2 has count 1 initially. If release() is invoked when maybeDirty() is forced,
        // mergeChild returns early because childCount == currState.count.
        child2.release();
        assertEquals(1, root.size());
    }
}