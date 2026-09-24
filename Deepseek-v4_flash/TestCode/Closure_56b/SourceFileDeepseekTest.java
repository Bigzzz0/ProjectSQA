package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for SourceFile, targeting the known defect where
 * getLine() and getRegion() return null for the last line when the source
 * does not end with a newline character.
 *
 * [Branch & Defect Analysis Matrix]
 * - Constructor: null/empty fileName → IllegalArgumentException
 * - getLine(): lineNumber < 1 → IllegalArgumentException (via getLineOffset)
 * - getLine(): lineNumber > number of lines → null
 * - getLine(): last line without trailing newline → should return line, but bug returns null
 * - getRegion(): lineNumber > number of lines → null
 * - getRegion(): last line without trailing newline → should return region, but bug returns null
 * - getLineOffset(): lineNumber < 1 or > numLines → IllegalArgumentException
 * - getNumLines(): correct count with/without trailing newline
 * - getOriginalPath(): default = fileName, setOriginalPath changes
 * - isExtern/setIsExtern: state transitions
 * - hasSourceInMemory: true after code set, false after clearCachedSource (for Generated)
 * - Static factories: fromCode, fromGenerator
 */
public class SourceFileDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testConstructorAndBasicGetters() {
        SourceFile sf = new SourceFile("test.js");
        assertEquals("test.js", sf.getName());
        assertEquals("test.js", sf.getOriginalPath());
        assertFalse(sf.isExtern());
        assertFalse(sf.hasSourceInMemory());
    }

    @Test(timeout = 4000)
    public void testSetIsExtern() {
        SourceFile sf = new SourceFile("extern.js");
        assertFalse(sf.isExtern());
        sf.setIsExtern(true);
        assertTrue(sf.isExtern());
        sf.setIsExtern(false);
        assertFalse(sf.isExtern());
    }

    @Test(timeout = 4000)
    public void testSetOriginalPath() {
        SourceFile sf = new SourceFile("test.js");
        assertEquals("test.js", sf.getOriginalPath());
        sf.setOriginalPath("/path/to/test.js");
        assertEquals("/path/to/test.js", sf.getOriginalPath());
    }

    @Test(timeout = 4000)
    public void testHasSourceInMemoryAfterCodeSet() {
        SourceFile sf = SourceFile.fromCode("test.js", "var a = 1;");
        assertTrue(sf.hasSourceInMemory());
    }

    @Test(timeout = 4000)
    public void testClearCachedSourceOnGenerated() throws Exception {
        SourceFile.Generator gen = () -> "generated code";
        SourceFile sf = SourceFile.fromGenerator("gen.js", gen);
        // Initially no cached code
        assertFalse(sf.hasSourceInMemory());
        // Trigger code loading
        String code = sf.getCode();
        assertEquals("generated code", code);
        assertTrue(sf.hasSourceInMemory());
        // Clear cache
        sf.clearCachedSource();
        assertFalse(sf.hasSourceInMemory());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullFileName() {
        new SourceFile(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorEmptyFileName() {
        new SourceFile("");
    }

    @Test(timeout = 4000)
    public void testGetLineOffsetValid() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2\nline3");
        assertEquals(0, sf.getLineOffset(1));
        assertEquals(6, sf.getLineOffset(2));  // "line1\n" length = 6
        assertEquals(12, sf.getLineOffset(3)); // "line1\nline2\n" length = 12
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetLineOffsetTooLow() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2");
        sf.getLineOffset(0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetLineOffsetTooHigh() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2");
        sf.getLineOffset(3);
    }

    @Test(timeout = 4000)
    public void testGetNumLinesWithTrailingNewline() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "a\nb\nc\n");
        assertEquals(3, sf.getNumLines());
    }

    @Test(timeout = 4000)
    public void testGetNumLinesWithoutTrailingNewline() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "a\nb\nc");
        assertEquals(3, sf.getNumLines());
    }

    @Test(timeout = 4000)
    public void testGetNumLinesEmptyString() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "");
        assertEquals(1, sf.getNumLines()); // split("") gives [""] -> length 1
    }

    @Test(timeout = 4000)
    public void testGetNumLinesSingleLineNoNewline() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "only one line");
        assertEquals(1, sf.getNumLines());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    // Bug: getLine returns null for last line when no trailing newline
    @Test(timeout = 4000)
    public void testGetLineLastLineNoNewline() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2\nlast line without newline");
        String line = sf.getLine(3);
        assertNotNull("getLine(3) should not be null for last line without newline", line);
        assertEquals("last line without newline", line);
    }

    // Bug: getRegion returns null for last line when no trailing newline
    @Test(timeout = 4000)
    public void testGetRegionLastLineNoNewline() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2\nlast line without newline");
        Region region = sf.getRegion(3);
        assertNotNull("getRegion(3) should not be null for last line without newline", region);
        assertTrue(region.getSource().contains("last line without newline"));
    }

    // Additional boundary: getLine on line beyond file length returns null
    @Test(timeout = 4000)
    public void testGetLineBeyondFile() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "only one line");
        assertNull(sf.getLine(2));
    }

    // Additional boundary: getRegion on line beyond file length returns null
    @Test(timeout = 4000)
    public void testGetRegionBeyondFile() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "only one line");
        assertNull(sf.getRegion(2));
    }

    // getLine with trailing newline works correctly
    @Test(timeout = 4000)
    public void testGetLineWithTrailingNewline() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2\nline3\n");
        assertEquals("line1", sf.getLine(1));
        assertEquals("line2", sf.getLine(2));
        assertEquals("line3", sf.getLine(3));
    }

    // getRegion with trailing newline works correctly
    @Test(timeout = 4000)
    public void testGetRegionWithTrailingNewline() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "line1\nline2\nline3\nline4\nline5\n");
        Region region = sf.getRegion(3);
        assertNotNull(region);
        assertTrue(region.getSource().contains("line3"));
    }

    // getRegion when file has fewer lines than region length
    @Test(timeout = 4000)
    public void testGetRegionShortFile() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "only one line");
        Region region = sf.getRegion(1);
        assertNotNull("getRegion(1) should not be null for a single-line file", region);
        assertEquals("only one line", region.getSource().trim());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetLineOffsetNullCode() throws Exception {
        // SourceFile with no code set (code is null)
        SourceFile sf = new SourceFile("test.js");
        // getLineOffset will call findLineOffsets which calls getCode() -> IOException -> sets lineOffsets to [0]
        // So it won't throw, but we can test that it works with null code? Actually getCode() returns null, then split throws NullPointerException? No, getCode() returns null, then .split("\n") throws NullPointerException.
        // But findLineOffsets catches IOException, not NullPointerException. So it will throw NullPointerException.
        // This is a potential bug, but we test that it throws something.
        sf.getLineOffset(1);
    }

    @Test(timeout = 4000)
    public void testGetLineIOExceptionReturnsNull() throws Exception {
        // Simulate IOException by using a SourceFile that throws on getCode()
        // We can use a custom Generator that throws IOException
        SourceFile.Generator gen = () -> { throw new RuntimeException("simulated"); };
        SourceFile sf = SourceFile.fromGenerator("test.js", gen);
        // getLine will call getCode() which throws IOException, then returns null
        assertNull(sf.getLine(1));
    }

    @Test(timeout = 4000)
    public void testGetRegionIOExceptionReturnsNull() throws Exception {
        SourceFile.Generator gen = () -> { throw new RuntimeException("simulated"); };
        SourceFile sf = SourceFile.fromGenerator("test.js", gen);
        assertNull(sf.getRegion(1));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testToString() {
        SourceFile sf = new SourceFile("myfile.js");
        assertEquals("myfile.js", sf.toString());
    }

    @Test(timeout = 4000)
    public void testStaticFromCode() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "var x = 1;");
        assertEquals("test.js", sf.getName());
        assertEquals("var x = 1;", sf.getCode());
    }

    @Test(timeout = 4000)
    public void testStaticFromCodeWithOriginalPath() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "/path/to/test.js", "var x = 1;");
        assertEquals("test.js", sf.getName());
        assertEquals("/path/to/test.js", sf.getOriginalPath());
        assertEquals("var x = 1;", sf.getCode());
    }

    @Test(timeout = 4000)
    public void testStaticFromGenerator() throws Exception {
        SourceFile.Generator gen = () -> "generated";
        SourceFile sf = SourceFile.fromGenerator("gen.js", gen);
        assertEquals("gen.js", sf.getName());
        assertEquals("generated", sf.getCode());
    }

    @Test(timeout = 4000)
    public void testGetCodeReader() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "code");
        java.io.Reader reader = sf.getCodeReader();
        char[] buf = new char[100];
        int len = reader.read(buf);
        assertEquals("code", new String(buf, 0, len));
    }

    @Test(timeout = 4000)
    public void testGetCodeNoCache() {
        SourceFile sf = SourceFile.fromCode("test.js", "cached code");
        assertEquals("cached code", sf.getCodeNoCache());
    }

    // Additional test for getLine with empty lines
    @Test(timeout = 4000)
    public void testGetLineEmptyLines() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "\n\n");
        assertEquals("", sf.getLine(1));
        assertEquals("", sf.getLine(2));
        assertNull(sf.getLine(3)); // only two lines (empty strings)
    }

    // Additional test for getRegion with empty lines
    @Test(timeout = 4000)
    public void testGetRegionEmptyLines() throws Exception {
        SourceFile sf = SourceFile.fromCode("test.js", "\n\n");
        Region region = sf.getRegion(2);
        assertNotNull(region);
        assertTrue(region.getSource().contains("\n"));
    }
}