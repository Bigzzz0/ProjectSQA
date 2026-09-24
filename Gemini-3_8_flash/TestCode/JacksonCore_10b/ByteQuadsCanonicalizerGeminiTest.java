/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer
 *
 * Targeted Decision Branches & Defects:
 * 1. Defect Issue 207 (ArrayIndexOutOfBoundsException: 256):
 *    - Occurs when long names (>= 4 quads) cause expansion of `_hashArea` in `_appendLongName()`.
 *    - Because `_hashArea.length` becomes larger than `_hashSize << 3`, the check
 *      `if (_spilloverEnd >= hashArea.length)` allows spillover to exceed the allocated slots for `_names`
 *      (which is only `_hashSize << 1`), leading to `_names[offset >> 2]` throwing ArrayIndexOutOfBoundsException.
 * 2. Hash Collisions & Tiered Cascading Lookups:
 *    - Primary slots: `findName(q1)`, `findName(q1, q2)`, `findName(q1, q2, q3)`, `findName(int[], len)`
 *    - Secondary slots: 1 collision, verified through `_secondaryStart`
 *    - Tertiary slots: 2+ collisions, verified through `_tertiaryStart` and bucket shifts
 *    - Spill-over area: fallthrough to `_spilloverStart` up to `_spilloverEnd`
 *    - Empty slots (len == 0): short-circuit branch return null
 * 3. Long Name Verification Branches:
 *    - `_verifyLongName`: cases 4, 5, 6, 7, 8, and default (`_verifyLongName2` loop)
 *    - Long name mismatches triggering false returns
 * 4. Lifecycle, Rehash, and Copy-On-Write:
 *    - Child creation via `makeChild(flags)` with intern and failOnDoS features
 *    - Merging via `release()` and `mergeChild()` (when dirty vs clean, count match, > MAX_ENTRIES_FOR_REUSE)
 *    - `rehash()` when count > 50% with spillover or count > 80%
 *    - Rehash boundary where newSize > MAX_T_SIZE calling `nukeSymbols(true)`
 * 5. Hash Calculation Boundary & Guard Paths:
 *    - `calcHash(int)`
 *    - `calcHash(int, int)`
 *    - `calcHash(int, int, int)`
 *    - `calcHash(int[], len)` including `len < 4` IllegalArgumentException
 * 6. Spillover DoS detection:
 *    - `_reportTooManyCollisions()` triggered when `_hashSize > 1024` and spillover full
 */

package com.fasterxml.jackson.core.sym;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;

public class ByteQuadsCanonicalizerGeminiTest {

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & Lookups
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testBasicAddAndFind1Quad() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(12345);
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());

        assertEquals(0, child.size());
        assertNull(child.findName(100));

        String added = child.addName("single", 100);
        assertEquals("single", added);
        assertEquals(1, child.size());
        assertEquals("single", child.findName(100));
        assertNull(child.findName(101));
    }

    @Test(timeout = 4000)
    public void testBasicAddAndFind2Quads() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(54321);
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());

        assertNull(child.findName(10, 20));
        String added = child.addName("two", 10, 20);
        assertEquals("two", added);
        assertEquals(1, child.size());
        assertEquals("two", child.findName(10, 20));
        assertNull(child.findName(10, 21));
        assertNull(child.findName(11, 20));

        // Test branch in addName where q2 == 0
        String addedZero = child.addName("two_zero", 99, 0);
        assertEquals("two_zero", addedZero);
        assertEquals("two_zero", child.findName(99, 0));
    }

    @Test(timeout = 4000)
    public void testBasicAddAndFind3Quads() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(111);
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());

        assertNull(child.findName(1, 2, 3));
        String added = child.addName("three", 1, 2, 3);
        assertEquals("three", added);
        assertEquals(1, child.size());
        assertEquals("three", child.findName(1, 2, 3));
        assertNull(child.findName(1, 2, 4));
        assertNull(child.findName(1, 9, 3));
        assertNull(child.findName(9, 2, 3));
    }

    @Test(timeout = 4000)
    public void testAddAndFindWithArrayDelegation() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(777);
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());

        int[] q1 = new int[]{55};
        assertEquals("oneA", child.addName("oneA", q1, 1));
        assertEquals("oneA", child.findName(q1, 1));

        int[] q2 = new int[]{55, 66};
        assertEquals("twoA", child.addName("twoA", q2, 2));
        assertEquals("twoA", child.findName(q2, 2));

        int[] q3 = new int[]{55, 66, 77};
        assertEquals("threeA", child.addName("threeA", q3, 3));
        assertEquals("threeA", child.findName(q3, 3));
    }

    @Test(timeout = 4000)
    public void testLongNamesLength4to8AndAbove() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(999);
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());

        // Test lengths 4, 5, 6, 7, 8 (switch cases in _verifyLongName)
        for (int len = 4; len <= 9; ++len) {
            int[] q = new int[len];
            for (int i = 0; i < len; ++i) {
                q[i] = (len * 100) + i;
            }
            String name = "len" + len;
            assertEquals(name, child.addName(name, q, len));
            assertEquals(name, child.findName(q, len));

            // Verify mismatch
            int[] mismatch = q.clone();
            mismatch[len - 1] += 1;
            assertNull(child.findName(mismatch, len));
        }
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis & Lifecycle/Sharing
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testRootCreationAndStateInspection() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot();
        assertNotNull(root);
        assertEquals(0, root.size());
        assertEquals(64, root.bucketCount());
        assertFalse(root.maybeDirty());
        assertTrue(root.hashSeed() != 0);

        String info = root.toString();
        assertTrue(info.contains("ByteQuadsCanonicalizer"));
        assertEquals(0, root.primaryCount());
        assertEquals(0, root.secondaryCount());
        assertEquals(0, root.tertiaryCount());
        assertEquals(0, root.spilloverCount());
        assertEquals(0, root.totalCount());
    }

    @Test(timeout = 4000)
    public void testChildReleaseAndMergeBackToParent() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(1234);
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());

        child.addName("name1", 100);
        child.addName("name2", 200);
        assertTrue(child.maybeDirty());
        assertEquals(2, child.size());
        assertEquals(0, root.size());

        child.release();
        // After release, parent should have merged child state
        assertEquals(2, root.size());
        assertFalse(child.maybeDirty());

        // Second child should inherit merged symbols
        ByteQuadsCanonicalizer child2 = root.makeChild(JsonFactory.Feature.collectDefaults());
        assertEquals(2, child2.size());
        assertEquals("name1", child2.findName(100));
        assertEquals("name2", child2.findName(200));

        // Releasing clean child does nothing
        child2.release();
        assertEquals(2, root.size());
    }

    @Test(timeout = 4000)
    public void testChildReleaseWithMaxEntriesPurge() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(1);
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());

        // Add more than MAX_ENTRIES_FOR_REUSE (6000)
        for (int i = 0; i < 6005; ++i) {
            child.addName("item" + i, i + 1);
        }
        assertEquals(6005, child.size());

        child.release();
        // Should purge and reset table back to 0 entries because childCount > MAX_ENTRIES_FOR_REUSE
        assertEquals(0, root.size());
    }

    @Test(timeout = 4000)
    public void testInternConfiguration() {
        int flagsNoIntern = 0; // disable INTERN_FIELD_NAMES
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(42);
        ByteQuadsCanonicalizer child = root.makeChild(flagsNoIntern);

        String dynamicStr = new String("nonInternedStr");
        String added = child.addName(dynamicStr, 12345);
        assertSame(dynamicStr, added);
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Zone (Defects4J ground truth)
    /**********************************************************
     */

    /**
     * Targets Jackson-core defect testIssue207 (ArrayIndexOutOfBoundsException: 256).
     * When long names (quad length >= 4) are added to the table, `_appendLongName`
     * expands `_hashArea`. The bug in the defective version is that the spill-over
     * bounds check compares `_spilloverEnd >= hashArea.length`, ignoring that `_hashArea`
     * has expanded beyond the fixed hash table while `_names` array has NOT been expanded.
     * When enough collisions occur to advance `_spilloverEnd`, `_names[offset >> 2]`
     * throws ArrayIndexOutOfBoundsException on the defective version.
     */
    @Test(timeout = 4000)
    public void testDefectIssue207LongNameSpilloverOverflow() {
        // Use seed 1 matching the test failure scenario
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(1);
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());

        // 1. Add several long names to cause `_appendLongName` to expand `_hashArea`
        int[] longQ = new int[5];
        for (int i = 0; i < 30; ++i) {
            longQ[0] = i;
            longQ[1] = i + 1;
            longQ[2] = i + 2;
            longQ[3] = i + 3;
            longQ[4] = i + 4;
            child.addName("longName" + i, longQ, 5);
        }

        // 2. Add many 1-quad symbols designed to fill secondary/tertiary slots and spillover
        for (int i = 0; i < 150; ++i) {
            child.addName("symbol" + i, (i * 64) + 1);
        }

        // Verify retrieval without throwing ArrayIndexOutOfBoundsException
        for (int i = 0; i < 30; ++i) {
            longQ[0] = i;
            longQ[1] = i + 1;
            longQ[2] = i + 2;
            longQ[3] = i + 3;
            longQ[4] = i + 4;
            assertEquals("longName" + i, child.findName(longQ, 5));
        }
        for (int i = 0; i < 150; ++i) {
            assertEquals("symbol" + i, child.findName((i * 64) + 1));
        }
    }

    /**
     * Targets synthetic collision lookups across secondary, tertiary, and spillover areas
     * to verify bucket shifting and cascading retrieval integrity.
     */
    @Test(timeout = 4000)
    public void testCascadingLookupThroughSecondaryTertiaryAndSpillover() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(100);
        ByteQuadsCanonicalizer sym = root.makeChild(JsonFactory.Feature.collectDefaults());

        // Add names: 1-quad, 2-quad, 3-quad, and multi-quad
        for (int i = 1; i <= 60; ++i) {
            sym.addName("k1_" + i, i);
            sym.addName("k2_" + i, i, i * 2);
            sym.addName("k3_" + i, i, i * 2, i * 3);
            sym.addName("k4_" + i, new int[]{i, i + 1, i + 2, i + 3}, 4);
        }

        // Verify lookups hit primary, secondary, tertiary, or spillover accurately
        for (int i = 1; i <= 60; ++i) {
            assertEquals("k1_" + i, sym.findName(i));
            assertEquals("k2_" + i, sym.findName(i, i * 2));
            assertEquals("k3_" + i, sym.findName(i, i * 2, i * 3));
            assertEquals("k4_" + i, sym.findName(new int[]{i, i + 1, i + 2, i + 3}, 4));
        }

        assertTrue("Should have primary entries", sym.primaryCount() > 0);
        assertTrue("Should have secondary or tertiary or spillover entries",
                (sym.secondaryCount() + sym.tertiaryCount() + sym.spilloverCount()) > 0);
    }

    /*
    /**********************************************************
    /* Partition D: Exception, Defensive Guards & Calculations
    /**********************************************************
     */

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCalcHashWithInvalidQuadLengthThrows() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(123);
        root.calcHash(new int[]{1, 2, 3}, 3);
    }

    @Test(timeout = 4000)
    public void testCalcTertiaryShiftBoundaries() {
        assertEquals(4, ByteQuadsCanonicalizer._calcTertiaryShift(16));
        assertEquals(4, ByteQuadsCanonicalizer._calcTertiaryShift(64));
        assertEquals(4, ByteQuadsCanonicalizer._calcTertiaryShift(255));
        assertEquals(5, ByteQuadsCanonicalizer._calcTertiaryShift(256));
        assertEquals(5, ByteQuadsCanonicalizer._calcTertiaryShift(1024));
        assertEquals(6, ByteQuadsCanonicalizer._calcTertiaryShift(1025));
        assertEquals(6, ByteQuadsCanonicalizer._calcTertiaryShift(4096));
        assertEquals(7, ByteQuadsCanonicalizer._calcTertiaryShift(4097));
    }

    @Test(timeout = 4000)
    public void testRehashUnderHighVolume() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(1234567);
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());

        int initialBucketCount = child.bucketCount();
        // Insert enough items to cause rehash multiple times
        for (int i = 0; i < 500; ++i) {
            child.addName("item" + i, i, i + 1);
        }

        assertTrue("Bucket count should expand beyond initial", child.bucketCount() > initialBucketCount);
        assertEquals(500, child.size());

        // Verify all elements are still accessible after rehash
        for (int i = 0; i < 500; ++i) {
            assertEquals("item" + i, child.findName(i, i + 1));
        }
    }

    @Test(timeout = 4000)
    public void testDoSProtectionDisabledAllowsSpilloverWithoutCrash() {
        // Disabling FAIL_ON_SYMBOL_HASH_OVERFLOW
        int flags = JsonFactory.Feature.INTERN_FIELD_NAMES.getMask();
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(999);
        ByteQuadsCanonicalizer child = root.makeChild(flags);

        // Add many items to verify it handles collisions gracefully without throwing DoS exception
        for (int i = 0; i < 200; ++i) {
            child.addName("dos" + i, i);
        }
        assertEquals(200, child.size());
    }

    @Test(timeout = 4000)
    public void testFindNameShortCircuitBranches() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(12345);
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.collectDefaults());

        // Empty table lookups should hit the len == 0 short-circuit branches
        assertNull(child.findName(123));
        assertNull(child.findName(123, 456));
        assertNull(child.findName(123, 456, 789));
        assertNull(child.findName(new int[]{1, 2, 3, 4}, 4));
    }
}