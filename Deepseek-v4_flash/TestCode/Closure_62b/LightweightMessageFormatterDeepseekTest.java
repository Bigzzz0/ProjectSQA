package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.base.Preconditions;
import com.google.javascript.jscomp.SourceExcerptProvider.SourceExcerpt;
import com.google.javascript.jscomp.SourceExcerptProvider.ExcerptFormatter;
import com.google.javascript.jscomp.SourceExcerptProvider.Region;

/**
 * White-box tests for LightweightMessageFormatter, targeting maximum coverage
 * and the known defect with caret placement for lines ending with a space.
 */
public class LightweightMessageFormatterDeepseekTest {

  /*
   * [Branch & Defect Analysis Matrix]
   * 
   * Targeted branches in format():
   * - source == null / source != null
   * - error.sourceName == null / != null
   * - error.lineNumber > 0 / <= 0
   * - sourceExcerpt == null / != null
   * - excerpt.equals(LINE) && 0 <= charno && charno < sourceExcerpt.length()
   * - charno < 0, charno >= length, charno within bounds
   * - whitespace vs non-whitespace in padding loop
   * 
   * Defect: For lines ending with a space, when charno equals the line length,
   * the caret line is omitted. Also when charno is at the last character (space),
   * the caret might be misaligned in the defective version.
   * We directly test these cases.
   * 
   * Also covers LineNumberingFormatter.formatRegion branches.
   */

  // Helper stub for SourceExcerptProvider
  private static class StubSourceExcerptProvider implements SourceExcerptProvider {
    private final java.util.Map<String, String> lines = new java.util.HashMap<>();

    public void putLine(String sourceName, int lineNumber, String line) {
      lines.put(sourceName + ":" + lineNumber, line);
    }

    @Override
    public String getSourceLine(String sourceName, int lineNumber) {
      return lines.get(sourceName + ":" + lineNumber);
    }

    @Override
    public Region getSourceRegion(String sourceName, int lineNumber, int length) {
      String line = getSourceLine(sourceName, lineNumber);
      if (line == null) {
        return null;
      }
      return new SimpleRegion(line, lineNumber);
    }

    private static class SimpleRegion implements Region {
      private final String line;
      private final int lineNumber;

      SimpleRegion(String line, int lineNumber) {
        this.line = line;
        this.lineNumber = lineNumber;
      }

      @Override
      public String getSourceExcerpt() {
        return line;
      }

      @Override
      public int getBeginningLineNumber() {
        return lineNumber;
      }

      @Override
      public int getEndingLineNumber() {
        return lineNumber;
      }
    }
  }

  private static LightweightMessageFormatter createFormatter(StubSourceExcerptProvider provider) {
    return new LightweightMessageFormatter(provider, SourceExcerpt.LINE);
  }

  private static JSError createError(String sourceName, int lineNumber, int charno,
      CheckLevel level, String description) {
    return JSError.make(sourceName, lineNumber, charno, level, description);
  }

  // ---------- Partition A: Core Functional Logic ----------

  @Test(timeout = 4000)
  public void testFormatErrorNoSource() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = createError(null, 0, 0, CheckLevel.ERROR, "no source");
    assertEquals("ERROR - no source\n", formatter.formatError(error));
  }

  @Test(timeout = 4000)
  public void testFormatWarningNoSource() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = createError(null, 0, 0, CheckLevel.WARNING, "warning msg");
    assertEquals("WARNING - warning msg\n", formatter.formatWarning(error));
  }

  @Test(timeout = 4000)
  public void testFormatErrorWithSourceAndLine() {
    StubSourceExcerptProvider provider = new StubSourceExcerptProvider();
    provider.putLine("file.js", 10, "var x = 1;");
    LightweightMessageFormatter formatter = createFormatter(provider);
    JSError error = createError("file.js", 10, 7, CheckLevel.ERROR, "bad var");
    String expected = "file.js:10: ERROR - bad var\nvar x = 1;\n       ^\n";
    assertEquals(expected, formatter.formatError(error));
  }

  @Test(timeout = 4000)
  public void testFormatWarningWithSourceAndLine() {
    StubSourceExcerptProvider provider = new StubSourceExcerptProvider();
    provider.putLine("file.js", 2, "foo();");
    LightweightMessageFormatter formatter = createFormatter(provider);
    JSError error = createError("file.js", 2, 0, CheckLevel.WARNING, "unused");
    String expected = "file.js:2: WARNING - unused\nfoo();\n^\n";
    assertEquals(expected, formatter.formatWarning(error));
  }

  // ---------- Partition B: Boundary Value Analysis ----------

  @Test(timeout = 4000)
  public void testFormatErrorNoLineNumber() {
    StubSourceExcerptProvider provider = new StubSourceExcerptProvider();
    LightweightMessageFormatter formatter = createFormatter(provider);
    JSError error = createError("file.js", 0, 0, CheckLevel.ERROR, "no line");
    // lineNumber is not > 0, so no ":" and no line number in prefix
    String expected = "file.js: ERROR - no line\n";
    assertEquals(expected, formatter.formatError(error));
  }

  @Test(timeout = 4000)
  public void testFormatErrorNegativeCharno() {
    StubSourceExcerptProvider provider = new StubSourceExcerptProvider();
    provider.putLine("file.js", 1, "abc");
    LightweightMessageFormatter formatter = createFormatter(provider);
    JSError error = createError("file.js", 1, -1, CheckLevel.ERROR, "neg");
    // charno < 0, so no caret line is added
    String expected = "file.js:1: ERROR - neg\nabc\n";
    assertEquals(expected, formatter.formatError(error));
  }

  @Test(timeout = 4000)
  public void testFormatErrorCharnoBeyondLine() {
    StubSourceExcerptProvider provider = new StubSourceExcerptProvider();
    provider.putLine("file.js", 1, "abc");
    LightweightMessageFormatter formatter = createFormatter(provider);
    JSError error = createError("file.js", 1, 5, CheckLevel.ERROR, "beyond");
    // charno >= length, no caret line
    String expected = "file.js:1: ERROR - beyond\nabc\n";
    assertEquals(expected, formatter.formatError(error));
  }

  @Test(timeout = 4000)
  public void testFormatErrorCharnoAtEndOfLine() {
    // charno == length (line ends with a space) → should have caret at end, but defect omits it
    StubSourceExcerptProvider provider = new StubSourceExcerptProvider();
    provider.putLine("file.js", 1, "abc ");
    LightweightMessageFormatter formatter = createFormatter(provider);
    JSError error = createError("file.js", 1, 4, CheckLevel.ERROR, "end");
    // Expected: caret after the space
    String expected = "file.js:1: ERROR - end\nabc \n    ^\n";
    assertEquals(expected, formatter.formatError(error));
  }

  @Test(timeout = 4000)
  public void testFormatErrorCharnoOnSpaceAtEnd() {
    // charno == length-1, points to the trailing space
    StubSourceExcerptProvider provider = new StubSourceExcerptProvider();
    provider.putLine("file.js", 1, "abc ");
    LightweightMessageFormatter formatter = createFormatter(provider);
    JSError error = createError("file.js", 1, 3, CheckLevel.ERROR, "space");
    // caret should point to the space
    String expected = "file.js:1: ERROR - space\nabc \n   ^\n";
    assertEquals(expected, formatter.formatError(error));
  }

  @Test(timeout = 4000)
  public void testFormatErrorLineNotInProvider() {
    StubSourceExcerptProvider provider = new StubSourceExcerptProvider();
    LightweightMessageFormatter formatter = createFormatter(provider);
    JSError error = createError("missing.js", 1, 0, CheckLevel.ERROR, "no line");
    // line not found → sourceExcerpt null, no caret
    String expected = "missing.js:1: ERROR - no line\n";
    assertEquals(expected, formatter.formatError(error));
  }

  @Test(timeout = 4000)
  public void testFormatErrorSourceNameNull() {
    StubSourceExcerptProvider provider = new StubSourceExcerptProvider();
    LightweightMessageFormatter formatter = createFormatter(provider);
    JSError error = createError(null, 1, 0, CheckLevel.ERROR, "no source name");
    // sourceName null, so no prefix
    String expected = "ERROR - no source name\n";
    assertEquals(expected, formatter.formatError(error));
  }

  // ---------- Partition C: Defect-Targeted Branch Zone ----------
  // (space at end of line, charno == length and charno == length-1)

  @Test(timeout = 4000)
  public void testFormatErrorSpaceEndOfLine1() {
    // Exact defect regression: charno == length → caret must appear
    StubSourceExcerptProvider provider = new StubSourceExcerptProvider();
    provider.putLine("file.js", 1, "test ");
    LightweightMessageFormatter formatter = createFormatter(provider);
    JSError error = createError("file.js", 1, 5, CheckLevel.ERROR, "description here");
    String expected = "file.js:1: ERROR - description here\ntest \n     ^\n";
    assertEquals(expected, formatter.formatError(error));
  }

  @Test(timeout = 4000)
  public void testFormatErrorSpaceEndOfLine2() {
    // Caret on the trailing space itself
    StubSourceExcerptProvider provider = new StubSourceExcerptProvider();
    provider.putLine("file.js", 1, "test ");
    LightweightMessageFormatter formatter = createFormatter(provider);
    JSError error = createError("file.js", 1, 4, CheckLevel.ERROR, "description here");
    String expected = "file.js:1: ERROR - description here\ntest \n    ^\n";
    assertEquals(expected, formatter.formatError(error));
  }

  // ---------- Partition D: Exception & Defensive Guard Paths ----------

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testConstructorNullSourceThrows() {
    new LightweightMessageFormatter(null);
  }

  @Test(timeout = 4000)
  public void testFormatErrorWithWhitespaceBeforeCaret() {
    StubSourceExcerptProvider provider = new StubSourceExcerptProvider();
    provider.putLine("file.js", 1, "  foo");
    LightweightMessageFormatter formatter = createFormatter(provider);
    JSError error = createError("file.js", 1, 4, CheckLevel.ERROR, "indent");
    // charno=4 (points to 'o'? Actually 0:' ',1:' ',2:'f',3:'o',4:'o')
    // padding: for i=0,1 we copy spaces, for i=2,3 we put spaces, then '^'
    String expected = "file.js:1: ERROR - indent\n  foo\n    ^\n";
    assertEquals(expected, formatter.formatError(error));
  }

  @Test(timeout = 4000)
  public void testFormatErrorWithTab() {
    StubSourceExcerptProvider provider = new StubSourceExcerptProvider();
    provider.putLine("file.js", 1, "\tfoo");
    LightweightMessageFormatter formatter = createFormatter(provider);
    JSError error = createError("file.js", 1, 1, CheckLevel.ERROR, "tab");
    // charno=1 points to 'f'? Actually '\t' at index 0, 'f' at 1. Padding: for i=0 (tab) we copy tab, then '^'
    String expected = "file.js:1: ERROR - tab\n\tfoo\n\t^\n";
    assertEquals(expected, formatter.formatError(error));
  }

  // ---------- Partition E: Object Lifecycle & Contract Integrity ----------

  @Test(timeout = 4000)
  public void testWithoutSourceStaticReturnsFormatter() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    assertNotNull(formatter);
    JSError error = createError(null, 0, 0, CheckLevel.ERROR, "x");
    assertEquals("ERROR - x\n", formatter.formatError(error));
  }

  @Test(timeout = 4000)
  public void testLineNumberingFormatterRegionEmpty() {
    // Indirectly test via formatError with empty line? Actually formatRegion is used when excerpt is LINE and line is "", it returns null.
    StubSourceExcerptProvider provider = new StubSourceExcerptProvider();
    provider.putLine("file.js", 1, "");
    LightweightMessageFormatter formatter = createFormatter(provider);
    JSError error = createError("file.js", 1, 0, CheckLevel.ERROR, "empty");
    // Empty line → no sourceExcerpt (LineNumberingFormatter returns null), so no caret
    String expected = "file.js:1: ERROR - empty\n";
    assertEquals(expected, formatter.formatError(error));
  }

  // Test formatRegion indirectly with a line with trailing spaces via formatError
  @Test(timeout = 4000)
  public void testLineNumberingFormatterWithTrailingSpace() {
    // The LINE excerpt uses formatRegion, which preserves trailing spaces.
    StubSourceExcerptProvider provider = new StubSourceExcerptProvider();
    provider.putLine("file.js", 1, "foo ");
    LightweightMessageFormatter formatter = createFormatter(provider);
    JSError error = createError("file.js", 1, 3, CheckLevel.ERROR, "trail");
    // The sourceExcerpt shown is "foo " (with space). The caret line should reflect that.
    String expected = "file.js:1: ERROR - trail\nfoo \n   ^\n";
    assertEquals(expected, formatter.formatError(error));
  }
}