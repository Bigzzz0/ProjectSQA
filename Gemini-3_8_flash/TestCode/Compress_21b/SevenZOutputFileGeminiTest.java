package org.apache.commons.compress.archivers.sevenz;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Component: SevenZOutputFile
 *
 * Identified Defect (Defects4J Commons-Compress Ground Truth):
 * - Methods affected: writeBits(DataOutput, BitSet, int)
 * - Fault Mechanism: In writeBits, the bit-shifting logic decrements `shift` and checks
 *   `if (shift == 0)` after writing only 7 bits per byte (since shift begins at 7 and decreases
 *   to 0). Furthermore, the trailing padding condition `if (length > 0 && shift > 0)` outputs
 *   improperly aligned/padded bits.
 * - Observable failure symptoms when reading back with SevenZFile:
 *   - 6 empty files: java.io.IOException: Badly terminated header
 *   - 7 empty files: java.io.IOException: Unknown property 128
 *   - 8 empty files: java.io.IOException: Unknown property 192
 *   - 9 empty files: java.lang.ArrayIndexOutOfBoundsException
 *   - 6, 7, 8, 9 files (mixed empty/non-empty): Badly terminated header / Unknown property 128.
 *
 * Test Matrix & Branch Coverage Goals:
 * 1. Partition A: Core Functional Logic & Compression Formats
 *    - Supported codecs: SevenZMethod.COPY, SevenZMethod.LZMA2, SevenZMethod.DEFLATE, SevenZMethod.BZIP2
 *    - Single and multi-byte stream writes (write(int), write(byte[]), write(byte[], off, len))
 *    - Directory entry handling (entry.setDirectory(true))
 * 2. Partition B: Boundary Value Analysis (BVA) & Metadata BitSets
 *    - Zero-length write operations: write(byte[], 0, 0)
 *    - Variable-length unsigned integer encoding: writeUint64 boundary conditions
 *    - Windows Attributes, Access Times, Creation Times, Modification Times
 *    - Mixed metadata presence (all set vs subset set)
 * 3. Partition C: Defect-Targeted Zone (Bit packaging regressions)
 *    - N empty files (where N = 6, 7, 8, 9)
 *    - N mixed files (non-empty & empty files where N = 6, 7, 8, 9)
 * 4. Partition D: Exception Paths & Lifecycle Contracts
 *    - Invoking finish() multiple times (expected IOException)
 *    - Non-empty vs empty archive entry closing
 *    - Closing output file without writing entries
 */
public class SevenZOutputFileGeminiTest {

    private File tempArchiveFile;
    private final List<File> tempFilesToClean = new ArrayList<File>();

    @Before
    public void setUp() throws IOException {
        tempArchiveFile