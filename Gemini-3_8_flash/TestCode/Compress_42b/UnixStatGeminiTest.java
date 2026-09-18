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
package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target: UnixStat constants & integration with ZipArchiveEntry Unix mode handling.
 *
 * 1. Constant Values & Octal/Bit Representation:
 *    - PERM_MASK         = 07777   (0xFFF,  4095)
 *    - LINK_FLAG         = 0120000 (0xA000, 40960)
 *    - FILE_FLAG         = 0100000 (0x8000, 32768)
 *    - DIR_FLAG          = 040000  (0x4000, 16384)
 *    - DEFAULT_LINK_PERM = 0777    (0x1FF,  511)
 *    - DEFAULT_DIR_PERM  = 0755    (0x1ED,  493)
 *    - DEFAULT_FILE_PERM = 0644    (0x1A4,  420)
 *
 * 2. Bitwise Invariants & Disjointness:
 *    - Flag bits vs PERM_MASK: All flag bits (LINK_FLAG, FILE_FLAG, DIR_FLAG) must not overlap PERM_MASK.
 *    - Default permissions vs PERM_MASK: Fully contained within PERM_MASK.
 *    - Flag overlap anomaly: LINK_FLAG (0120000) & FILE_FLAG (0100000) == FILE_FLAG.
 *
 * 3. Defects4J Known Defect:
 *    - ZipArchiveEntryTest::isUnixSymlinkIsFalseIfMoreThanOneFlagIsSet
 *    - In defective versions, `isUnixSymlink()` checks `(getUnixMode() & LINK_FLAG) == LINK_FLAG`.
 *      When multiple flags (e.g., LINK_FLAG | FILE_FLAG or LINK_FLAG | DIR_FLAG) are set,
 *      the bitwise AND condition spuriously succeeds, returning true instead of false.
 * -----------------------------------------------------------------------------------------
 */
public class UnixStatGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstantExactValues() {
        assertEquals(07777, UnixStat.PERM_MASK);
        assertEquals(4095, UnixStat.PERM_MASK);
        assertEquals(0xFFF, UnixStat.PERM_MASK);

        assertEquals(0120000, UnixStat.LINK_FLAG);
        assertEquals(40960, UnixStat.LINK_FLAG);
        assertEquals(0xA000, UnixStat.LINK_FLAG);

        assertEquals(0100000, UnixStat.FILE_FLAG);
        assertEquals(32768, UnixStat.FILE_FLAG);
        assertEquals(0x8000, UnixStat.FILE_FLAG);

        assertEquals(040000, UnixStat.DIR_FLAG);
        assertEquals(16384, UnixStat.DIR_FLAG);
        assertEquals(0x4000, UnixStat.DIR_FLAG);

        assertEquals(0777, UnixStat.DEFAULT_LINK_PERM);
        assertEquals(511, UnixStat.DEFAULT_LINK_PERM);

        assertEquals(0755, UnixStat.DEFAULT_DIR_PERM);
        assertEquals(493, UnixStat.DEFAULT_DIR_PERM);

        assertEquals(0644, UnixStat.DEFAULT_FILE_PERM);
        assertEquals(420, UnixStat.DEFAULT_FILE_PERM);
    }

    @Test(timeout = 4000)
    public void testPermissionMaskIsolation() {
        assertEquals("LINK_FLAG must not leak into PERM_MASK",
                0, UnixStat.LINK_FLAG & UnixStat.PERM_MASK);
        assertEquals("FILE_FLAG must not leak into PERM_MASK",
                0, UnixStat.FILE_FLAG & UnixStat.PERM_MASK);
        assertEquals("DIR_FLAG must not leak into PERM_MASK",
                0, UnixStat.DIR_FLAG & UnixStat.PERM_MASK);
    }

    @Test(timeout = 4000)
    public void testDefaultPermissionsContainedInMask() {
        assertEquals("DEFAULT_LINK_PERM must fit in PERM_MASK",
                UnixStat.DEFAULT_LINK_PERM, UnixStat.DEFAULT_LINK_PERM & UnixStat.PERM_MASK);
        assertEquals("DEFAULT_DIR_PERM must fit in PERM_MASK",
                UnixStat.DEFAULT_DIR_PERM, UnixStat.DEFAULT_DIR_PERM & UnixStat.PERM_MASK);
        assertEquals("DEFAULT_FILE_PERM must fit in PERM_MASK",
                UnixStat.DEFAULT_FILE_PERM, UnixStat.DEFAULT_FILE_PERM & UnixStat.PERM_MASK);
    }

    @Test(timeout = 4000)
    public void testValidSymlinkFlagIntegration() {
        ZipArchiveEntry entry = new ZipArchiveEntry("symlink_entry");
        int mode = UnixStat.LINK_FLAG | UnixStat.DEFAULT_LINK_PERM;
        entry.setUnixMode(mode);

        assertEquals(mode, entry.getUnixMode());
        assertTrue("Entry with pure LINK_FLAG must be recognized as symlink", entry.isUnixSymlink());
        assertFalse("Entry with LINK_FLAG must not be directory", entry.isDirectory());
    }

    @Test(timeout = 4000)
    public void testValidDirectoryFlagIntegration() {
        ZipArchiveEntry entry = new ZipArchiveEntry("dir_entry/");
        int mode = UnixStat.DIR_FLAG | UnixStat.DEFAULT_DIR_PERM;
        entry.setUnixMode(mode);

        assertEquals(mode, entry.getUnixMode());
        assertFalse("Entry with DIR_FLAG must not be symlink", entry.isUnixSymlink());
        assertTrue("Entry with DIR_FLAG must be directory", entry.isDirectory());
    }

    @Test(timeout = 4000)
    public void testValidRegularFileFlagIntegration() {
        ZipArchiveEntry entry = new ZipArchiveEntry("regular_file");
        int mode = UnixStat.FILE_FLAG | UnixStat.DEFAULT_FILE_PERM;
        entry.setUnixMode(mode);

        assertEquals(mode, entry.getUnixMode());
        assertFalse("Entry with FILE_FLAG must not be symlink", entry.isUnixSymlink());
        assertFalse("Entry with FILE_FLAG must not be directory", entry.isDirectory());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testZeroMode() {
        ZipArchiveEntry entry = new ZipArchiveEntry("zero_mode");
        entry.setUnixMode(0);

        assertEquals(0, entry.getUnixMode());
        assertFalse("Mode 0 must not be a symlink", entry.isUnixSymlink());
        assertFalse("Mode 0 without trailing slash must not be directory", entry.isDirectory());
    }

    @Test(timeout = 4000)
    public void testOnlyPermissionBitsSet() {
        ZipArchiveEntry entry = new ZipArchiveEntry("perm_only");
        entry.setUnixMode(UnixStat.PERM_MASK);

        assertEquals(UnixStat.PERM_MASK, entry.getUnixMode());
        assertFalse("PERM_MASK alone must not be recognized as symlink", entry.isUnixSymlink());
        assertFalse("PERM_MASK alone must not be recognized as directory", entry.isDirectory());
    }

    @Test(timeout = 4000)
    public void testFlagBitIntersections() {
        assertEquals("DIR_FLAG and FILE_FLAG must be mutually disjoint",
                0, UnixStat.DIR_FLAG & UnixStat.FILE_FLAG);
        assertEquals("DIR_FLAG and LINK_FLAG must be mutually disjoint",
                0, UnixStat.DIR_FLAG & UnixStat.LINK_FLAG);
        // Note: LINK_FLAG (0120000) shares bit 15 with FILE_FLAG (0100000) in Unix stat layout
        assertEquals("LINK_FLAG includes FILE_FLAG bit in Unix standard encoding",
                UnixStat.FILE_FLAG, UnixStat.LINK_FLAG & UnixStat.FILE_FLAG);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J issue where ZipArchiveEntry.isUnixSymlink() incorrectly returns
     * true if LINK_FLAG and FILE_FLAG are simultaneously set.
     */
    @Test(timeout = 4000)
    public void isUnixSymlinkIsFalseIfMoreThanOneFlagIsSet() {
        ZipArchiveEntry zae = new ZipArchiveEntry("foo");
        zae.setUnixMode(UnixStat.LINK_FLAG | UnixStat.FILE_FLAG);
        assertFalse("isUnixSymlink must be false if more than one flag is set", zae.isUnixSymlink());
    }

    @Test(timeout = 4000)
    public void testIsUnixSymlinkIsFalseIfLinkAndDirFlagsAreSet() {
        ZipArchiveEntry zae = new ZipArchiveEntry("foo_dir_link");
        zae.setUnixMode(UnixStat.LINK_FLAG | UnixStat.DIR_FLAG);
        assertFalse("isUnixSymlink must be false if DIR_FLAG is combined with LINK_FLAG", zae.isUnixSymlink());
    }

    @Test(timeout = 4000)
    public void testIsUnixSymlinkIsFalseIfAllFlagsAreSet() {
        ZipArchiveEntry zae = new ZipArchiveEntry("foo_all");
        zae.setUnixMode(UnixStat.LINK_FLAG | UnixStat.FILE_FLAG | UnixStat.DIR_FLAG);
        assertFalse("isUnixSymlink must be false if all flags are set", zae.isUnixSymlink());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testExtractPermissionsUsingMask() {
        int fullMode = UnixStat.LINK_FLAG | 0751;
        int extractedPerm = fullMode & UnixStat.PERM_MASK;
        assertEquals(0751, extractedPerm);

        int fileMode = UnixStat.FILE_FLAG | UnixStat.DEFAULT_FILE_PERM;
        assertEquals(UnixStat.DEFAULT_FILE_PERM, fileMode & UnixStat.PERM_MASK);

        int dirMode = UnixStat.DIR_FLAG | UnixStat.DEFAULT_DIR_PERM;
        assertEquals(UnixStat.DEFAULT_DIR_PERM, dirMode & UnixStat.PERM_MASK);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    private static class UnixStatTestConsumer implements UnixStat {
        public int getLinkFlag() {
            return LINK_FLAG;
        }
        public int getFileFlag() {
            return FILE_FLAG;
        }
        public int getDirFlag() {
            return DIR_FLAG;
        }
        public int getPermMask() {
            return PERM_MASK;
        }
    }

    @Test(timeout = 4000)
    public void testInterfaceImplementationContract() {
        UnixStat consumer = new UnixStatTestConsumer();
        assertNotNull(consumer);
        assertTrue(consumer instanceof UnixStat);

        UnixStatTestConsumer concrete = (UnixStatTestConsumer) consumer;
        assertEquals(UnixStat.LINK_FLAG, concrete.getLinkFlag());
        assertEquals(UnixStat.FILE_FLAG, concrete.getFileFlag());
        assertEquals(UnixStat.DIR_FLAG, concrete.getDirFlag());
        assertEquals(UnixStat.PERM_MASK, concrete.getPermMask());
    }
}