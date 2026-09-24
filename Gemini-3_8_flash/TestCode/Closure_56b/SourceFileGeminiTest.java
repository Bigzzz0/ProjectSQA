package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 *
 * 1. Defect-Targeted Branch Zone (Closure Defect: js.indexOf('\n', pos) == -1 in getLine):
 *    - In SourceFile#getLine(int lineNumber): When a file does not terminate with a newline character,
 *      attempting to get the last line causes `js.indexOf('\n', pos)` to evaluate to -1.
 *      The buggy implementation unconditionally returns `null` instead of returning the remaining
 *      contents between `pos` and EOF (`js.substring(pos)`).
 *    - Target Tests: `testDefectGetLineLastLineWithoutTrailingNewline()`, `testDefectSingleLineWithoutNewline()`
 *
 * 2. Core Functional Branches & Equivalence Classes:
 *    - Construction & Validation: null fileName, empty fileName, valid fileName.
 *    - Line Offset Calculation (`findLineOffsets`, `getLineOffset`, `getNumLines`):
 *      * Single line, multiline, empty file, boundary offset verification.
 *      * Invalid line offset query: lineno < 1, lineno > lineOffsets.length.
 *    - Line Querying (`getLine`):
 *      * Querying forward sequentially (`lineNumber >= lastLine`).
 *      * Querying backward (`lineNumber < lastLine`) to ensure lastOffset/lastLine reset branch is exercised.
 *      * Querying beyond EOF (`lineNumber > totalLines`).
 *    - Region Extraction (`getRegion`):
 *      * lineNumber < 1 / line 1 (startLine = 1).
 *      * File ending with '\n' vs file ending without '\n' for `end == -1` branch.
 *      * File with > 5 lines where region terminates with `end != -1`.
 *      * lineNumber >= endLine returning null.
 *    - Subclasses & Lazy Loading:
 *      * Preloaded: constructor with 2 and 3 args, originalPath propagation.
 *      * Generated: custom Generator, caching verification, clearCachedSource cache eviction & regeneration.
 *      * OnDisk: lazy loading (`getCode`), FileReader before cache vs StringReader after cache (`getCodeReader`),
 *        custom charset handling, clearCachedSource cache invalidation.
 *    - Static Factory Methods:
 *      * fromFile(String), fromFile(String, Charset), fromFile(File), fromFile(File, Charset).
 *      * fromCode(String, String), fromCode(String, String, String).
 *      * fromInputStream(String, InputStream), fromInputStream(String, String, InputStream).
 *      * fromReader(String, Reader), fromGenerator(String, Generator).
 *    - State & Attributes:
 *      * isExtern / setIsExtern.
 *      * originalPath fallback to fileName when null.
 *      * toString returning fileName.
 *      * Serialization / Deserialization integrity.
 */
public class SourceFileGeminiTest {

  // ==========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
  // ==========================================================================

  /**
   * Targets the defect where SourceFile#getLine(int) returns null when the requested line
   * is the last line of the file and does NOT end with a newline character ('\n').
   * Ground truth: JSCompilerSourceExcerptProviderTest#testExceptNoNewLine.
   */
  @Test(timeout = 4000)
  public void testDefectGetLineLastLineWithoutTrailingNewline() {
    String content = "foo1:first line\nfoo2:second line\nfoo2:third line";
    SourceFile sf = SourceFile.fromCode("test.js", content);

    assertEquals("foo1:first line", sf.getLine(1));
    assertEquals("foo2:second line", sf.getLine(2));
    // Bug: in defective version, js.indexOf('\n', pos) == -1 causes it to return null!
    assertEquals("foo2:third line", sf.getLine(3));
  }

  /**
   * Targets single line source file without a trailing newline.
   */
  @Test(timeout = 4000)
  public void testDefectSingleLineWithoutNewline() {
    String content = "var x = 10;";
    SourceFile sf = SourceFile.fromCode("inline.js", content);
    assertEquals("var x = 10;", sf.getLine(1));
  }

  // ==========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ==========================================================================

  @Test(timeout = 4000)
  public void testBasicPropertiesAndExternFlag() {
    SourceFile sf = SourceFile.fromCode("my_file.js", "var a = 1;");
    assertEquals("my_file.js", sf.getName());
    assertEquals("my_file.js", sf.toString());
    assertFalse(sf.isExtern());

    sf.setIsExtern(true);
    assertTrue(sf.isExtern());
    sf.setIsExtern(false);
    assertFalse(sf.isExtern());
  }

  @Test(timeout = 4000)
  public void testOriginalPathFallbackAndOverride() {
    SourceFile sf = SourceFile.fromCode("generated.js", "var a = 1;");
    assertEquals("generated.js", sf.getOriginalPath());

    sf.setOriginalPath("original/path.js");
    assertEquals("original/path.js", sf.getOriginalPath());

    SourceFile sfWithOriginal = SourceFile.fromCode("gen.js", "orig.js", "var b = 2;");
    assertEquals("gen.js", sfWithOriginal.getName());
    assertEquals("orig.js", sfWithOriginal.getOriginalPath());
  }

  @Test(timeout = 4000)
  public void testLineOffsetsAndNumLines() throws IOException {
    String code = "line1\nline22\n\nline4";
    SourceFile sf = SourceFile.fromCode("offsets.js", code);

    assertEquals(4, sf.getNumLines());
    assertEquals(0, sf.getLineOffset(1));
    assertEquals(6, sf.getLineOffset(2));
    assertEquals(13, sf.getLineOffset(3));
    assertEquals(14, sf.getLineOffset(4));
    assertEquals(code, sf.getCode());
    assertEquals(code, sf.getCodeNoCache());
  }

  @Test(timeout = 4000)
  public void testGetCodeReader() throws IOException {
    String code = "var x = 42;";
    SourceFile sf = SourceFile.fromCode("reader.js", code);
    Reader reader = sf.getCodeReader();
    assertNotNull(reader);

    char[] buf = new char[code.length()];
    int read = reader.read(buf);
    assertEquals(code.length(), read);
    assertEquals(code, new String(buf));
  }

  @Test(timeout = 4000)
  public void testGetLineAscendingAndDescending() {
    String code = "line1\nline2\nline3\nline4\n";
    SourceFile sf = SourceFile.fromCode("traversal.js", code);

    // Forward traversal
    assertEquals("line1", sf.getLine(1));
    assertEquals("line3", sf.getLine(3));

    // Backward traversal (lineNumber < lastLine branch)
    assertEquals("line2", sf.getLine(2));
    assertEquals("line1", sf.getLine(1));

    // Beyond EOF
    assertNull(sf.getLine(5));
    assertNull(sf.getLine(100));
  }

  @Test(timeout = 4000)
  public void testGetRegionMiddleLines() {
    String code = "1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n";
    SourceFile sf = SourceFile.fromCode("region.js", code);

    // Region length is 5. For line 5: startLine = max(1, 5 - 3 + 1) = 3.
    // endLine iterates 5 times: 3, 4, 5, 6, 7.
    Region region = sf.getRegion(5);
    assertNotNull(region);
    assertEquals(3, region.getBeginningLineNumber());
    assertEquals(8, region.getEndingLineNumber());
    assertEquals("3\n4\n5\n6\n7\n", region.getSourceExcerpt());
  }

  @Test(timeout = 4000)
  public void testGetRegionFirstLine() {
    String code = "line1\nline2\nline3\n";
    SourceFile sf = SourceFile.fromCode("region_start.js", code);

    Region region = sf.getRegion(1);
    assertNotNull(region);
    assertEquals(1, region.getBeginningLineNumber());
    assertEquals(4, region.getEndingLineNumber());
    assertEquals("line1\nline2\nline3\n", region.getSourceExcerpt());
  }

  @Test(timeout = 4000)
  public void testGetRegionWithoutTrailingNewline() {
    String code = "line1\nline2\nline3";
    SourceFile sf = SourceFile.fromCode("region_no_eol.js", code);

    Region region = sf.getRegion(2);
    assertNotNull(region);
    assertEquals(1, region.getBeginningLineNumber());
    assertEquals(4, region.getEndingLineNumber());
    assertEquals("line1\nline2\nline3", region.getSourceExcerpt());
  }

  @Test(timeout = 4000)
  public void testGetRegionBeyondBoundsReturnsNull() {
    String code = "line1\nline2\n";
    SourceFile sf = SourceFile.fromCode("region_oob.js", code);

    assertNull(sf.getRegion(10));
  }

  @Test(timeout = 4000)
  public void testGeneratedSourceFile() throws IOException {
    final int[] invocationCount = new int[] {0};
    SourceFile.Generator generator = new SourceFile.Generator() {
      @Override
      public String getCode() {
        invocationCount[0]++;
        return "var generated = " + invocationCount[0] + ";";
      }
    };

    SourceFile sf = SourceFile.fromGenerator("gen.js", generator);
    assertFalse(sf.hasSourceInMemory());

    // First access invokes generator
    assertEquals("var generated = 1;", sf.getCode());
    assertTrue(sf.hasSourceInMemory());
    assertEquals(1, invocationCount[0]);

    // Cached access should not invoke generator again
    assertEquals("var generated = 1;", sf.getCode());
    assertEquals(1, invocationCount[0]);

    // Clear cache and retrieve again
    sf.clearCachedSource();
    assertFalse(sf.hasSourceInMemory());
    assertEquals("var generated = 2;", sf.getCode());
    assertEquals(2, invocationCount[0]);
  }

  @Test(timeout = 4000)
  public void testOnDiskSourceFile() throws IOException {
    File tempFile = File.createTempFile("closure_test", ".js");
    tempFile.deleteOnExit();

    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
      fos.write("var disk = 123;\n".getBytes(StandardCharsets.UTF_8));
    }

    SourceFile sf = SourceFile.fromFile(tempFile);
    assertFalse(sf.hasSourceInMemory());
    assertEquals(StandardCharsets.UTF_8, ((SourceFile.OnDisk) sf).getCharset());

    // getCodeReader before loading code into memory reads from FileReader
    Reader readerBeforeLoad = sf.getCodeReader();
    assertNotNull(readerBeforeLoad);
    char[] buf = new char[5];
    int read = readerBeforeLoad.read(buf);
    assertEquals(5, read);
    assertEquals("var d", new String(buf));
    readerBeforeLoad.close();

    // Now load into memory
    assertEquals("var disk = 123;\n", sf.getCode());
    assertTrue(sf.hasSourceInMemory());

    // getCodeReader after loading into memory reads from memory
    Reader readerAfterLoad = sf.getCodeReader();
    assertNotNull(readerAfterLoad);
    char[] buf2 = new char[10];
    int read2 = readerAfterLoad.read(buf2);
    assertEquals(10, read2);
    assertEquals("var disk =", new String(buf2));
    readerAfterLoad.close();

    // Clear cached source
    sf.clearCachedSource();
    assertFalse(sf.hasSourceInMemory());
  }

  @Test(timeout = 4000)
  public void testOnDiskWithExplicitCharset() throws IOException {
    File tempFile = File.createTempFile("closure_charset", ".js");
    tempFile.deleteOnExit();

    String content = "var iso = 'äöü';\n";
    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
      fos.write(content.getBytes(StandardCharsets.ISO_8859_1));
    }

    SourceFile sf = SourceFile.fromFile(tempFile.getPath(), StandardCharsets.ISO_8859_1);
    assertEquals(content, sf.getCode());
    assertEquals(StandardCharsets.ISO_8859_1, ((SourceFile.OnDisk) sf).getCharset());

    SourceFile sf2 = SourceFile.fromFile(tempFile, StandardCharsets.ISO_8859_1);
    assertEquals(content, sf2.getCode());

    SourceFile sf3 = SourceFile.fromFile(tempFile.getPath());
    assertNotNull(sf3);
  }

  @Test(timeout = 4000)
  public void testFromInputStream() throws IOException {
    String content = "function foo() { return 'hello'; }";
    ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    SourceFile sf = SourceFile.fromInputStream("stream.js", bais);
    assertEquals("stream.js", sf.getName());
    assertEquals(content, sf.getCode());

    ByteArrayInputStream bais2 = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    SourceFile sf2 = SourceFile.fromInputStream("stream2.js", "origStream.js", bais2);
    assertEquals("stream2.js", sf2.getName());
    assertEquals("origStream.js", sf2.getOriginalPath());
    assertEquals(content, sf2.getCode());
  }

  @Test(timeout = 4000)
  public void testFromReader() throws IOException {
    String content = "function bar() { return 100; }";
    StringReader sr = new StringReader(content);
    SourceFile sf = SourceFile.fromReader("reader.js", sr);
    assertEquals(content, sf.getCode());
  }

  // ==========================================================================
  // Partition B: Boundary Value Analysis & Extremes
  // ==========================================================================

  @Test(timeout = 4000)
  public void testEmptySourceFile() throws IOException {
    SourceFile sf = SourceFile.fromCode("empty.js", "");
    assertEquals("", sf.getCode());
    assertEquals(1, sf.getNumLines());
    assertEquals(0, sf.getLineOffset(1));
    assertNull(sf.getLine(1));
  }

  @Test(timeout = 4000)
  public void testOnlyNewlineSourceFile() {
    SourceFile sf = SourceFile.fromCode("newline.js", "\n");
    assertEquals(1, sf.getNumLines());
    assertEquals("", sf.getLine(1));
    assertNull(sf.getLine(2));
  }

  @Test(timeout = 4000)
  public void testConsecutiveNewlines() {
    SourceFile sf = SourceFile.fromCode("newlines.js", "\n\n\n");
    assertEquals(1, sf.getNumLines());
    assertEquals("", sf.getLine(1));
    assertEquals("", sf.getLine(2));
    assertEquals("", sf.getLine(3));
    assertNull(sf.getLine(4));
  }

  @Test(timeout = 4000)
  public void testGetRegionNearEndOfSmallFile() {
    String code = "1\n2\n";
    SourceFile sf = SourceFile.fromCode("small.js", code);
    Region region = sf.getRegion(2);
    assertNotNull(region);
    assertEquals(1, region.getBeginningLineNumber());
    assertEquals(3, region.getEndingLineNumber());
    assertEquals("1\n2", region.getSourceExcerpt());
  }

  // ==========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // ==========================================================================

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testConstructorNullFileName() {
    new SourceFile(null);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testConstructorEmptyFileName() {
    new SourceFile("");
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetLineOffsetZero() {
    SourceFile sf = SourceFile.fromCode("test.js", "var x = 1;");
    sf.getLineOffset(0);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetLineOffsetNegative() {
    SourceFile sf = SourceFile.fromCode("test.js", "var x = 1;");
    sf.getLineOffset(-5);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetLineOffsetBeyondMax() {
    SourceFile sf = SourceFile.fromCode("test.js", "var x = 1;");
    sf.getLineOffset(2);
  }

  @Test(timeout = 4000)
  public void testClearCachedSourceBaseNoOp() {
    SourceFile sf = SourceFile.fromCode("test.js", "var x = 1;");
    assertTrue(sf.hasSourceInMemory());
    // Preloaded does not clear cache because it's not regenerated
    sf.clearCachedSource();
    assertTrue(sf.hasSourceInMemory());
  }

  // ==========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // ==========================================================================

  @Test(timeout = 4000)
  public void testSerializationOnDisk() throws Exception {
    File tempFile = File.createTempFile("serial_test", ".js");
    tempFile.deleteOnExit();
    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
      fos.write("var serialized = true;".getBytes(StandardCharsets.UTF_8));
    }

    SourceFile.OnDisk onDisk = new SourceFile.OnDisk(tempFile, StandardCharsets.UTF_8);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(onDisk);
    }

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    try (ObjectInputStream ois = new ObjectInputStream(bais)) {
      Object deserialized = ois.readObject();
      assertTrue(deserialized instanceof SourceFile.OnDisk);
      SourceFile.OnDisk restored = (SourceFile.OnDisk) deserialized;
      assertEquals(onDisk.getName(), restored.getName());
      assertEquals(StandardCharsets.UTF_8, restored.getCharset());
      assertEquals("var serialized = true;", restored.getCode());
    }
  }

  @Test(timeout = 4000)
  public void testSerializationPreloaded() throws Exception {
    SourceFile preloaded = SourceFile.fromCode("test.js", "orig.js", "var a = 1;");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(preloaded);
    }

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    try (ObjectInputStream ois = new ObjectInputStream(bais)) {
      Object deserialized = ois.readObject();
      assertTrue(deserialized instanceof SourceFile);
      SourceFile restored = (SourceFile) deserialized;
      assertEquals("test.js", restored.getName());
      assertEquals("orig.js", restored.getOriginalPath());
      assertEquals("var a = 1;", restored.getCode());
    }
  }
}