package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.LightweightMessageFormatter.LineNumberingFormatter;
import com.google.javascript.jscomp.SourceExcerptProvider.SourceExcerpt;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 * Target: com.google.javascript.jscomp.LightweightMessageFormatter
 *
 * 1. Constructor Guard & Null checks:
 *    - LightweightMessageFormatter(SourceExcerptProvider)
 *    - LightweightMessageFormatter(SourceExcerptProvider, SourceExcerpt) with null source -> NPE
 *    - withoutSource() factory -> source is null, excerpt is LINE
 *
 * 2. Message Formatting Dimensions:
 *    - error vs warning (formatError vs formatWarning)
 *    - error.sourceName: null vs non-null
 *    - error.lineNumber: <= 0 (no line number printed) vs > 0 (printed as ":<line>:")
 *    - sourceExcerpt: null vs non-null (LINE vs REGION excerpt)
 *    - charno boundaries (Caret '^' positioning):
 *        * charno < 0 -> no caret
 *        * charno == 0 -> caret at first char
 *        * 0 < charno < length -> caret under char
 *        * charno == length -> caret at missing end-of-line position (DEFECT CLOSURE-86/DEFECTS4J TARGET)
 *        * charno > length -> no caret
 *    - whitespace preservation in caret padding:
 *        * tabs '\t' preserved in caret line
 *        * other whitespace preserved
 *
 * 3. LineNumberingFormatter (ExcerptFormatter):
 *    - formatLine(String, int) -> identity check
 *    - formatRegion(null) -> null
 *    - formatRegion(empty string) -> null
 *    - formatRegion single line without trailing '\n'
 *    - formatRegion single line with trailing '\n' (empty line branch)
 *    - formatRegion multi-line with variable digit width line numbers (padding alignment)
 */
public class LightweightMessageFormatterGeminiTest {

  private static final DiagnosticType FOO_TYPE =
      DiagnosticType.error("FOO_ERROR", "description here");
  private static final DiagnosticType WARN_TYPE =
      DiagnosticType.warning("FOO_WARN", "warning description here");

  private SourceExcerptProvider createSourceProvider(final String sourceName, final String sourceCode) {
    return new SourceExcerptProvider() {
      @Override
      public String getSourceLine(String name, int lineNumber) {
        if (sourceName != null && sourceName.equals(name)) {
          return sourceCode;
        }
        return null;
      }

      @Override
      public Region getSourceRegion(String name, int lineNumber) {
        if (sourceName != null && sourceName.equals(name)) {
          return new Region() {
            @Override
            public String getSourceExcerpt() {
              return sourceCode;
            }

            @Override
            public int getBeginningLineNumber() {
              return 1;
            }

            @Override
            public int getEndingLineNumber() {
              return 1;
            }
          };
        }
        return null;
      }
    };
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testFormatErrorWithoutSource() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = JSError.make("test.js", 10, 5, FOO_TYPE);

    String formatted = formatter.formatError(error);
    assertEquals("test.js:10: ERROR - description here\n", formatted);
  }

  @Test(timeout = 4000)
  public void testFormatWarningWithoutSource() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError warning = JSError.make("test.js", 10, 5, WARN_TYPE);

    String formatted = formatter.formatWarning(warning);
    assertEquals("test.js:10: WARNING - warning description here\n", formatted);
  }

  @Test(timeout = 4000)
  public void testFormatErrorWithSourceLineAndCaret() {
    String code = "var x = 1;";
    SourceExcerptProvider provider = createSourceProvider("test.js", code);
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, 4, FOO_TYPE);

    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - description here\nvar x = 1;\n    ^\n", formatted);
  }

  @Test(timeout = 4000)
  public void testFormatErrorWithTabsPreservedInCaret() {
    String code = "\t\tvar x = 1;";
    SourceExcerptProvider provider = createSourceProvider("test.js", code);
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, 4, FOO_TYPE);

    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - description here\n\t\tvar x = 1;\n\t\t  ^\n", formatted);
  }

  @Test(timeout = 4000)
  public void testFormatRegionWithExcerptFormatter() {
    final String code = "line 1\nline 2\n";
    SourceExcerptProvider provider = new SourceExcerptProvider() {
      @Override
      public String getSourceLine(String sourceName, int lineNumber) {
        return null;
      }

      @Override
      public Region getSourceRegion(String sourceName, int lineNumber) {
        return new Region() {
          @Override
          public String getSourceExcerpt() {
            return code;
          }

          @Override
          public int getBeginningLineNumber() {
            return 1;
          }

          @Override
          public int getEndingLineNumber() {
            return 2;
          }
        };
      }
    };

    LightweightMessageFormatter formatter =
        new LightweightMessageFormatter(provider, SourceExcerpt.REGION);
    JSError error = JSError.make("script.js", 1, 0, FOO_TYPE);

    String formatted = formatter.formatError(error);
    String expected =
        "script.js:1: ERROR - description here\n"
            + "  1| line 1\n"
            + "  2| line 2\n";
    assertEquals(expected, formatted);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testNullSourceName() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = JSError.make(null, 1, 2, FOO_TYPE);

    String formatted = formatter.formatError(error);
    assertEquals("ERROR - description here\n", formatted);
  }

  @Test(timeout = 4000)
  public void testNegativeAndZeroLineNumber() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();

    JSError error0 = JSError.make("test.js", 0, 2, FOO_TYPE);
    assertEquals("test.js: ERROR - description here\n", formatter.formatError(error0));

    JSError errorNeg = JSError.make("test.js", -1, 2, FOO_TYPE);
    assertEquals("test.js: ERROR - description here\n", formatter.formatError(errorNeg));
  }

  @Test(timeout = 4000)
  public void testNegativeCharnoProducesNoCaret() {
    String code = "var x = 1;";
    SourceExcerptProvider provider = createSourceProvider("test.js", code);
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, -1, FOO_TYPE);

    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - description here\nvar x = 1;\n", formatted);
  }

  @Test(timeout = 4000)
  public void testCharnoZeroPutsCaretAtBeginning() {
    String code = "var x = 1;";
    SourceExcerptProvider provider = createSourceProvider("test.js", code);
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, 0, FOO_TYPE);

    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - description here\nvar x = 1;\n^\n", formatted);
  }

  @Test(timeout = 4000)
  public void testCharnoGreaterThanLengthProducesNoCaret() {
    String code = "foo";
    SourceExcerptProvider provider = createSourceProvider("test.js", code);
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    // Length is 3, charno is 10 (strictly greater than length)
    JSError error = JSError.make("test.js", 1, 10, FOO_TYPE);

    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - description here\nfoo\n", formatted);
  }

  @Test(timeout = 4000)
  public void testSourceProviderReturnsNullExcerpt() {
    SourceExcerptProvider provider = createSourceProvider("different.js", "content");
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("test.js", 1, 2, FOO_TYPE);

    String formatted = formatter.formatError(error);
    assertEquals("test.js:1: ERROR - description here\n", formatted);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets the defect where charno == sourceExcerpt.length() failed to emit
   * a caret. In the defective implementation:
   *   charno < sourceExcerpt.length()
   * was used instead of:
   *   charno <= sourceExcerpt.length()
   */
  @Test(timeout = 4000)
  public void testFormatErrorSpaceEndOfLine1() {
    String code = "assert (1;";
    SourceExcerptProvider provider = createSourceProvider("javascript/complex.js", code);
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("javascript/complex.js", 1, 10, FOO_TYPE);

    String actual = formatter.formatError(error);
    String expected =
        "javascript/complex.js:1: ERROR - description here\n"
            + "assert (1;\n"
            + "          ^\n";
    assertEquals(expected, actual);
  }

  /**
   * Targets the defect when charno points past a trailing space at the end of the line.
   */
  @Test(timeout = 4000)
  public void testFormatErrorSpaceEndOfLine2() {
    String code = "assert ";
    SourceExcerptProvider provider = createSourceProvider("javascript/complex.js", code);
    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("javascript/complex.js", 1, 7, FOO_TYPE);

    String actual = formatter.formatError(error);
    String expected =
        "javascript/complex.js:1: ERROR - description here\n"
            + "assert \n"
            + "       ^\n";
    assertEquals(expected, actual);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testConstructorNullSourceOneArgThrowsNPE() {
    new LightweightMessageFormatter(null);
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testConstructorNullSourceTwoArgsThrowsNPE() {
    new LightweightMessageFormatter(null, SourceExcerpt.LINE);
  }

  // =========================================================================
  // Partition E: LineNumberingFormatter Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testLineNumberingFormatterFormatLine() {
    LineNumberingFormatter formatter = new LineNumberingFormatter();
    String line = "function test() {}";
    assertEquals(line, formatter.formatLine(line, 42));
  }

  @Test(timeout = 4000)
  public void testLineNumberingFormatterNullRegion() {
    LineNumberingFormatter formatter = new LineNumberingFormatter();
    assertNull(formatter.formatRegion(null));
  }

  @Test(timeout = 4000)
  public void testLineNumberingFormatterEmptyRegion() {
    LineNumberingFormatter formatter = new LineNumberingFormatter();
    Region region = new Region() {
      @Override
      public String getSourceExcerpt() {
        return "";
      }

      @Override
      public int getBeginningLineNumber() {
        return 1;
      }

      @Override
      public int getEndingLineNumber() {
        return 1;
      }
    };
    assertNull(formatter.formatRegion(region));
  }

  @Test(timeout = 4000)
  public void testLineNumberingFormatterSingleLineNoNewline() {
    LineNumberingFormatter formatter = new LineNumberingFormatter();
    Region region = new Region() {
      @Override
      public String getSourceExcerpt() {
        return "single line";
      }

      @Override
      public int getBeginningLineNumber() {
        return 5;
      }

      @Override
      public int getEndingLineNumber() {
        return 5;
      }
    };
    assertEquals("  5| single line", formatter.formatRegion(region));
  }

  @Test(timeout = 4000)
  public void testLineNumberingFormatterMultiDigitPadding() {
    LineNumberingFormatter formatter = new LineNumberingFormatter();
    Region region = new Region() {
      @Override
      public String getSourceExcerpt() {
        return "line 9\nline 10\nline 11";
      }

      @Override
      public int getBeginningLineNumber() {
        return 9;
      }

      @Override
      public int getEndingLineNumber() {
        return 11;
      }
    };
    String expected =
        "   9| line 9\n"
            + "  10| line 10\n"
            + "  11| line 11";
    assertEquals(expected, formatter.formatRegion(region));
  }

  @Test(timeout = 4000)
  public void testLineNumberingFormatterTrailingNewlineTrimming() {
    LineNumberingFormatter formatter = new LineNumberingFormatter();
    Region region = new Region() {
      @Override
      public String getSourceExcerpt() {
        return "line 1\n";
      }

      @Override
      public int getBeginningLineNumber() {
        return 1;
      }

      @Override
      public int getEndingLineNumber() {
        return 1;
      }
    };
    assertEquals("  1| line 1", formatter.formatRegion(region));
  }
}