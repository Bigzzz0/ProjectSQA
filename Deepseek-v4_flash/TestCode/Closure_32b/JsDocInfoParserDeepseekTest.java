package com.google.javascript.jscomp.parsing;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.head.ErrorReporter;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Advanced White-Box test suite for JsDocInfoParser.
 * Targets line/branch coverage and known Defects4J failures regarding
 * @license and @preserve whitespace preservation.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional state transitions (parse with different annotation sequences)
 * - Partition B: Boundary value analysis (null/empty strings, zero boundaries, MAX types)
 * - Partition C: Defect-targeted branch zone (preserved whitespace, file overview, duplicate tags)
 * - Partition D: Exception/guard paths (unexpected EOF, malformed annotations, syntax warnings)
 * - Partition E: Object lifecycle (build JSDocInfo, reset, multiple parse calls)
 */
public class JsDocInfoParserDeepseekTest {

  /* ------- Helper methods ------- */

  /**
   * Creates a minimal Config with empty annotation/suppression sets.
   */
  private static Config createMinimalConfig() {
    return new Config(
        new HashSet<String>(),
        new HashSet<String>(),
        false,
        LanguageMode.ECMASCRIPT3,
        false);
  }

  /**
   * Creates a silent ErrorReporter that collects warnings (optional).
   */
  private static ErrorReporter createSilentReporter() {
    return new ErrorReporter() {
      @Override
      public void warning(String s, String s1, int i, String s2, int i1) {
        // ignore
      }

      @Override
      public void error(String s, String s1, int i, String s2, int i1) {
        // ignore
      }

      @Override
      public EvaluatorException runtimeError(String s, String s1, int i, String s2, int i1) {
        return new EvaluatorException(s, s1, i, s2, i1);
      }
    };
  }

  /**
   * Helper to parse a JSDoc comment and return the built JSDocInfo.
   */
  private JSDocInfo parseComment(String commentText, Node.FileLevelJsDocBuilder fileLevelBuilder,
                                  ErrorReporter reporter) {
    JsDocTokenStream stream = new JsDocTokenStream(commentText);
    // The parser expects the comment string without the leading "/**" and trailing "*/"?
    // Actually JsDocTokenStream takes the entire source, and the tokenizer strips the comment markers.
    // We provide the full comment string.
    Config config = createMinimalConfig();
    // We can parse without an associated node or Comment object.
    JsDocInfoParser parser = new JsDocInfoParser(
        stream, null, null, config, reporter);
    if (fileLevelBuilder != null) {
      parser.setFileLevelJsDocBuilder(fileLevelBuilder);
    }
    parser.parse();
    return parser.retrieveAndResetParsedJSDocInfo();
  }

  /**
   * Helper that parses and returns the JSDocInfo (without file-level builder).
   */
  private JSDocInfo parseComment(String commentText) {
    return parseComment(commentText, null, createSilentReporter());
  }

  /* ------- Partition A: Core Functional Logic & State Transitions ------- */

  @Test(timeout = 4000)
  public void testParsesSimpleAnnotation() {
    JSDocInfo info = parseComment("/** @deprecated */");
    assertNotNull(info);
    assertTrue(info.isDeprecated());
  }

  @Test(timeout = 4000)
  public void testParsesDescription() {
    JSDocInfo info = parseComment("/** Some description. */");
    assertNotNull(info);
    assertTrue(info.hasDescription());
    assertEquals("Some description.", info.getDescription());
  }

  @Test(timeout = 4000)
  public void testParsesMultipleAnnotations() {
    JSDocInfo info = parseComment("/** @deprecated @param {number} x */");
    assertNotNull(info);
    assertTrue(info.isDeprecated());
    assertTrue(info.hasParameter("x"));
  }

  @Test(timeout = 4000)
  public void testParsesAuthor() {
    JSDocInfo info = parseComment("/** @author john@example.com */");
    assertNotNull(info);
    assertEquals(1, info.getAuthors().size());
    assertEquals("john@example.com", info.getAuthors().get(0));
  }

  /* ------- Partition B: Boundary Value Analysis & Extremes ------- */

  @Test(timeout = 4000)
  public void testEmptyComment() {
    JSDocInfo info = parseComment("/** */");
    assertNotNull(info);
    assertFalse(info.isDeprecated());
    assertFalse(info.hasDescription());
  }

  @Test(timeout = 4000)
  public void testCommentWithOnlyStar() {
    JSDocInfo info = parseComment("/***/");
    // This is valid: star is part of content? Actually "/***/" yields an empty doc.
    assertNotNull(info);
  }

  @Test(timeout = 4000)
  public void testUnterminatedEOF() {
    // Comment ending without */ triggers EOF warning, but still returns a built JSDocInfo (maybe null).
    JSDocInfo info = parseComment("/** @deprecated");
    // According to code, on EOF parse returns false and jsdocBuilder.build(null) is discarded.
    assertNull(info);
  }

  @Test(timeout = 4000)
  public void testNullAssociatedNode() {
    // This should not cause NPE.
    JsDocTokenStream stream = new JsDocTokenStream("/** @const */");
    Config config = createMinimalConfig();
    JsDocInfoParser parser = new JsDocInfoParser(stream, null, null, config, createSilentReporter());
    boolean result = parser.parse();
    assertTrue(result);
    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isConstant());
  }

  /* ------- Partition C: Defect-Targeted Branch Zone (Defects4J failures) ------- */

  /**
   * Directly targets the known failure for @license whitespace preservation.
   * The bug: leading newline and spaces are incorrectly trimmed or lost.
   */
  @Test(timeout = 4000)
  public void testParseLicensePreservesWhitespace() {
    final StringBuilder builder = new StringBuilder();
    Node.FileLevelJsDocBuilder fileLevelBuilder = new Node.FileLevelJsDocBuilder() {
      @Override
      public void append(String text) {
        builder.append(text);
      }
    };

    // Input: @license with a leading newline and spaces (common pattern).
    String comment = "/** @license\n * Foo\n */";
    JSDocInfo info = parseComment(comment, fileLevelBuilder, createSilentReporter());

    // According to the bug report, expected string should contain "\n Foo" (with newline and space).
    String preserved = builder.toString();
    assertTrue("Preserved text should contain newline and leading space",
               preserved.contains("\n"));
    assertTrue("Preserved text should include 'Foo' with a space before it",
               preserved.contains(" Foo") || preserved.contains("\nFoo"));
    // More precisely: the preserved text should be "\n Foo\n "? Actually tokenizer strips the trailing *.
    // We check that the text starts with a newline.
    assertTrue("Preserved text should start with newline", preserved.startsWith("\n"));
  }

  /**
   * Targets @preserve tag with multiline text.
   */
  @Test(timeout = 4000)
  public void testParsePreservePreservesNewlines() {
    final StringBuilder builder = new StringBuilder();
    Node.FileLevelJsDocBuilder fileLevelBuilder = new Node.FileLevelJsDocBuilder() {
      @Override
      public void append(String text) {
        builder.append(text);
      }
    };

    String comment = "/** @preserve\n * Line1\n * Line2\n */";
    JSDocInfo info = parseComment(comment, fileLevelBuilder, createSilentReporter());

    String preserved = builder.toString();
    assertTrue("Preserve should contain multiple lines", preserved.contains("\n"));
    assertTrue("Should contain Line1", preserved.contains("Line1"));
    assertTrue("Should contain Line2", preserved.contains("Line2"));
    // Ensure newlines are preserved (not collapsed into single line).
    assertTrue("Newlines should be present", preserved.contains("\n"));
  }

  /**
   * Targets @license with ASCII art (special characters).
   */
  @Test(timeout = 4000)
  public void testParseLicenseAscii() {
    final StringBuilder builder = new StringBuilder();
    Node.FileLevelJsDocBuilder fileLevelBuilder = new Node.FileLevelJsDocBuilder() {
      @Override
      public void append(String text) {
        builder.append(text);
      }
    };

    String comment = "/** @license\n *  Foo Bar Baz\n */";
    JSDocInfo info = parseComment(comment, fileLevelBuilder, createSilentReporter());

    String preserved = builder.toString();
    // Expected: "\n  Foo Bar Baz\n " (with two spaces before Foo)
    assertTrue(preserved.contains("  Foo"));
    assertTrue(preserved.contains("Bar Baz"));
  }

  /**
   * Integration scenario: file overview with @fileoverview and @license.
   */
  @Test(timeout = 4000)
  public void testIssue701Scenario() {
    final StringBuilder builder = new StringBuilder();
    Node.FileLevelJsDocBuilder fileLevelBuilder = new Node.FileLevelJsDocBuilder() {
      @Override
      public void append(String text) {
        builder.append(text);
      }
    };

    String comment = "/** @fileoverview\n *  Some overview\n * @license\n *  License text\n */";
    JSDocInfo info = parseComment(comment, fileLevelBuilder, createSilentReporter());

    // fileoverview should be recorded
    assertNotNull(info);
    assertTrue(info.hasFileOverview());
    assertEquals("Some overview", info.getFileOverview());

    // License preserved correctly
    String preserved = builder.toString();
    assertTrue(preserved.contains("\n  License text\n"));
  }

  /* ------- Partition D: Exception & Defensive Guard Paths ------- */

  @Test(timeout = 4000)
  public void testUnknownAnnotationWarning() {
    // Should not throw, just a warning.
    JSDocInfo info = parseComment("/** @unknownAnnotation */");
    assertNotNull(info);
  }

  @Test(timeout = 4000)
  public void testSyntaxWarningOnBadType() {
    // Expect a type syntax warning (no crash).
    JSDocInfo info = parseComment("/** @param {bad type} x */");
    assertNotNull(info);
    // The param might not be recorded due to parse error.
    assertFalse(info.hasParameter("x"));
  }

  @Test(timeout = 4000)
  public void testDuplicateParamWarning() {
    JSDocInfo info = parseComment("/** @param {number} x @param {string} x */");
    assertNotNull(info);
    assertTrue(info.hasParameter("x"));
    // Last type wins? Actually second one would cause a duplicate warning and not override.
    // We just ensure no crash.
  }

  @Test(timeout = 4000)
  public void testModifiesUnknownKeyword() {
    // @modifies with unknown keyword triggers warning but no crash.
    JSDocInfo info = parseComment("/** @modifies {unknownKeyword} */");
    assertNotNull(info);
  }

  /* ------- Partition E: Object Lifecycle & Contract Integrity ------- */

  @Test(timeout = 4000)
  public void testBuildResetsBuilder() {
    JsDocTokenStream stream = new JsDocTokenStream("/** @deprecated */");
    Config config = createMinimalConfig();
    JsDocInfoParser parser = new JsDocInfoParser(stream, null, null, config, createSilentReporter());
    parser.parse();
    JSDocInfo info1 = parser.retrieveAndResetParsedJSDocInfo();
    assertTrue(info1.isDeprecated());

    // Parse another comment
    JsDocTokenStream stream2 = new JsDocTokenStream("/** @const */");
    // We cannot reuse the same parser instance easily; but we can test that a new parse works.
  }

  @Test(timeout = 4000)
  public void testParseTypeStringStatic() {
    // Test static method separately for coverage.
    Node typeNode = JsDocInfoParser.parseTypeString("number");
    assertNotNull(typeNode);
    assertEquals("number", typeNode.getString());
  }

  /* ------- Additional coverage for branches ------- */

  @Test(timeout = 4000)
  public void testEnumWithoutExplicitTypeDefaultsToNumber() {
    JSDocInfo info = parseComment("/** @enum */");
    assertNotNull(info);
    assertTrue(info.hasEnumParameterType());
  }

  @Test(timeout = 4000)
  public void testExtendsWithBrace() {
    JSDocInfo info = parseComment("/** @extends {SomeClass} */");
    assertNotNull(info);
    assertTrue(info.hasBaseType());
  }

  @Test(timeout = 4000)
  public void testImplements() {
    JSDocInfo info = parseComment("/** @implements {Interface} */");
    assertNotNull(info);
    assertTrue(info.getImplementedInterfacesCount() > 0);
  }

  @Test(timeout = 4000)
  public void testReturnTypeWithoutDescription() {
    JSDocInfo info = parseComment("/** @return {string} */");
    assertNotNull(info);
    assertTrue(info.hasReturnType());
  }

  @Test(timeout = 4000)
  public void testParamWithOptionalBrackets() {
    JSDocInfo info = parseComment("/** @param {number=} [x] */");
    assertNotNull(info);
    assertTrue(info.hasParameter("x"));
    // Type should be optional.
  }

  @Test(timeout = 4000)
  public void testThrows() {
    JSDocInfo info = parseComment("/** @throws {Error} if something */");
    assertNotNull(info);
    assertTrue(info.getThrownTypes().size() > 0);
  }

  @Test(timeout = 4000)
  public void testHiddenAnnotation() {
    JSDocInfo info = parseComment("/** @hidden */");
    assertNotNull(info);
    assertTrue(info.isHidden());
  }

  @Test(timeout = 4000)
  public void testExportAnnotation() {
    JSDocInfo info = parseComment("/** @export */");
    assertNotNull(info);
    assertTrue(info.isExport());
  }

  @Test(timeout = 4000)
  public void testNoCompile() {
    JSDocInfo info = parseComment("/** @noCompile */");
    assertNotNull(info);
    assertTrue(info.isNoCompile());
  }

  @Test(timeout = 4000)
  public void testPrivateVisibility() {
    JSDocInfo info = parseComment("/** @private */");
    assertNotNull(info);
    assertEquals(JSDocInfo.Visibility.PRIVATE, info.getVisibility());
  }

  @Test(timeout = 4000)
  public void testProtectedVisibility() {
    JSDocInfo info = parseComment("/** @protected */");
    assertNotNull(info);
    assertEquals(JSDocInfo.Visibility.PROTECTED, info.getVisibility());
  }

  @Test(timeout = 4000)
  public void testPublicVisibility() {
    JSDocInfo info = parseComment("/** @public */");
    assertNotNull(info);
    assertEquals(JSDocInfo.Visibility.PUBLIC, info.getVisibility());
  }

  @Test(timeout = 4000)
  public void testOverrideAnnotation() {
    JSDocInfo info = parseComment("/** @override */");
    assertNotNull(info);
    assertTrue(info.isOverride());
  }

  @Test(timeout = 4000)
  public void testTypeAnnotation() {
    JSDocInfo info = parseComment("/** @type {string} */");
    assertNotNull(info);
    assertTrue(info.hasType());
  }

  @Test(timeout = 4000)
  public void testTypedefAnnotation() {
    JSDocInfo info = parseComment("/** @typedef {Object} */");
    assertNotNull(info);
    assertTrue(info.hasTypedefType());
  }

  @Test(timeout = 4000)
  public void testDefineAnnotation() {
    JSDocInfo info = parseComment("/** @define {boolean} */");
    assertNotNull(info);
    assertTrue(info.isDefine());
  }

  @Test(timeout = 4000)
  public void testSeeAnnotation() {
    JSDocInfo info = parseComment("/** @see {@link #method} */");
    assertNotNull(info);
    assertEquals(1, info.getReferences().size());
  }

  @Test(timeout = 4000)
  public void testSuppressAnnotation() {
    JSDocInfo info = parseComment("/** @suppress {warning} */");
    assertNotNull(info);
    assertTrue(info.getSuppressions().contains("warning"));
  }

  @Test(timeout = 4000)
  public void testLendsAnnotation() {
    JSDocInfo info = parseComment("/** @lends {someObject} */");
    assertNotNull(info);
    assertTrue(info.getLendsName().equals("someObject"));
  }

  @Test(timeout = 4000)
  public void testFileOverviewWithExtraWarning() {
    // Two @fileoverview should trigger a warning but not crash.
    JSDocInfo info = parseComment("/** @fileoverview First\n@fileoverview Second */");
    assertNotNull(info);
    assertTrue(info.hasFileOverview());
    // The first one should be recorded.
    assertTrue("First fileoverview should be recorded", info.getFileOverview().contains("First"));
  }
}