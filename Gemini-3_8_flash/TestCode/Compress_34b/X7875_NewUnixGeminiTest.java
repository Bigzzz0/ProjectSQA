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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.zip.ZipException;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: X7875_NewUnix
 *
 * Coverage Target Areas:
 * 1. Defect-Targeted Branch Zone:
 *    - getCentralDirectoryLength(): Must return ZipShort(0) because getCentralDirectoryData() is 0 bytes.
 *      Defect in target: getCentralDirectoryLength() returns getLocalFileDataLength(), failing with
 *      "expected:<0> but was:<5>".
 *
 * 2. Core Operational & State Transitions:
 *    - Default constructor initializes version=1, uid=1000, gid=1000.
 *    - getHeaderId(): returns 0x7875.
 *    - getUID() / setUID(): long conversion for small and large unsigned values.
 *    - getGID() / setGID(): long conversion for small and large unsigned values.
 *    - getLocalFileDataLength() and getLocalFileDataData(): little-endian serialization of variable sizes.
 *    - parseFromLocalFileData(): parsing version, uidSize, little-endian uid, gidSize, little-endian gid.
 *    - parseFromCentralDirectoryData(): no-op execution.
 *
 * 3. Boundary Value Analysis (BVA):
 *    - trimLeadingZeroesForceMinLength():
 *      * null array -> returns null
 *      * empty array -> returns 1-byte array of [0]
 *      * all-zero array -> returns 1-byte array of [0]
 *      * leading zeroes -> trims zeroes, preserves non-zero bytes
 *      * no leading zeroes -> preserves byte order and size
 *    - UID/GID extreme values: 0 (root), 1000 (typical user), 65534 (nobody), 2^31 - 1, 2^32 - 1 (unsigned max int).
 *    - parseFromLocalFileData with non-zero offset.
 *
 * 4. Exception & Defensive Guard Paths:
 *    - parseFromLocalFileData with truncated or insufficient byte array (ArrayIndexOutOfBoundsException).
 *
 * 5. Object Lifecycle & Contract Integrity:
 *    - equals: reflexive, symmetric, non-nullity, cross-type, value equality across version, uid, gid.
 *    - hashCode: consistency with equals, rotation-based spread.
 *    - clone(): independent clone preserves values; mutation of original does not alter clone.
 *    - Serializable: serialization and deserialization retains equivalence.
 *    - toString(): contains expected substring representation.
 */
public class X7875_NewUnixGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Directly reproduces the defect reported in Defects4J:
     * testParseReparse -> expected:<0> but was:<5>
     * Central directory length MUST be 0 since central directory data is always empty (0 bytes).
     */
    @Test(timeout = 4000)
    public void testDefectCentralDirectoryLengthMustBeZero() {
        X7875_NewUnix xf = new X7875_NewUnix();
        byte[] cdData = xf.getCentralDirectoryData();
        assertNotNull("Central directory data should not be null", cdData);
        assertEquals("Central directory data length must be 0", 0, cdData.length);

        ZipShort cdLength = xf.getCentralDirectoryLength();
        assertNotNull("Central directory length should not be null", cdLength);
        assertEquals("Central directory length must be 0 to match central directory data",
                0, cdLength.getValue());
    }

    /**
     * Checks that after modifying UID/GID, getCentralDirectoryLength() remains 0,
     * while getLocalFileDataLength() updates accordingly.
     */
    @Test(timeout = 4000)
    public void testDefectCentralDirectoryLengthRemainsZeroAfterModifications() {
        X7875_NewUnix xf = new X7875_NewUnix();
        xf.setUID(0);
        xf.setGID(0);

        // Local data size for UID=0, GID=0: 1 (version) + 1 (uidSize) + 1 (uid: 0) + 1 (gidSize) + 1 (gid: 0) = 5
        assertEquals(5, xf.getLocalFileDataLength().getValue());
        assertEquals(0, xf.getCentralDirectoryLength().getValue());

        xf.setUID(0xFFFFFFFFL);
        xf.setGID(0xFFFFFFFFL);
        // Local data size for 32-bit unsigned max: 1 + 1 + 4 + 1 + 4 = 11
        assertEquals(11, xf.getLocalFileDataLength().getValue());
        assertEquals(0, xf.getCentralDirectoryLength().getValue());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testHeaderId() {
        X7875_NewUnix xf = new X7875_NewUnix();
        assertEquals(new ZipShort(0x7875), xf.getHeaderId());
        assertEquals(0x7875, xf.getHeaderId().getValue());
    }

    @Test(timeout = 4000)
    public void testDefaultValues() {
        X7875_NewUnix xf = new X7875_NewUnix();
        assertEquals(1000L, xf.getUID());
        assertEquals(1000L, xf.getGID());

        // 1000 in hex is 0x03E8 (2 bytes).
        // Format: version (1 byte) + uidLen (1 byte) + uid (2 bytes) + gidLen (1 byte) + gid (2 bytes) = 7 bytes.
        assertEquals(7, xf.getLocalFileDataLength().getValue());

        byte[] expectedData = new byte[] {
            1,          // version
            2,          // uid length
            (byte) 0xE8, 0x03, // 1000 in little-endian
            2,          // gid length
            (byte) 0xE8, 0x03  // 1000 in little-endian
        };
        assertArrayEquals(expectedData, xf.getLocalFileDataData());
    }

    @Test(timeout = 4000)
    public void testSetAndGetUidGid() {
        X7875_NewUnix xf = new X7875_NewUnix();
        xf.setUID(500L);
        xf.setGID(600L);
        assertEquals(500L, xf.getUID());
        assertEquals(600L, xf.getGID());
    }

    @Test(timeout = 4000)
    public void testParseFromLocalFileData() throws ZipException {
        X7875_NewUnix xf = new X7875_NewUnix();
        byte[] data = new byte[] {
            1,                  // version
            2,                  // uid size
            (byte) 0xE8, 0x03,  // uid = 1000 in little-endian
            1,                  // gid size
            (byte) 0x2A         // gid = 42 in little-endian
        };

        xf.parseFromLocalFileData(data, 0, data.length);
        assertEquals(1000L, xf.getUID());
        assertEquals(42L, xf.getGID());
    }

    @Test(timeout = 4000)
    public void testParseFromLocalFileDataWithOffset() throws ZipException {
        X7875_NewUnix xf = new X7875_NewUnix();
        byte[] buffer = new byte[] {
            (byte) 0xFF, (byte) 0xFF, // prefix padding
            1,                        // version
            1,                        // uid size
            5,                        // uid = 5
            2,                        // gid size
            0x00, 0x01                // gid = 256
        };

        xf.parseFromLocalFileData(buffer, 2, buffer.length - 2);
        assertEquals(5L, xf.getUID());
        assertEquals(256L, xf.getGID());
    }

    @Test(timeout = 4000)
    public void testParseReparseConsistency() throws ZipException {
        X7875_NewUnix xf1 = new X7875_NewUnix();
        xf1.setUID(123456789L);
        xf1.setGID(98