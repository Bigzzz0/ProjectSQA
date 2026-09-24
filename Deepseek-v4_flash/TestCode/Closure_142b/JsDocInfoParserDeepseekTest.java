package com.google.javascript.jscomp.parsing;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.jscomp.parsing.Config;
import com.google.javascript.jscomp.parsing.JsDocTokenStream;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.jscomp.mozilla.rhino.ErrorReporter;
import com.google.javascript.jscomp.mozilla.rhino.NullErrorReporter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * White-box test suite for JsDocInfoParser.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional paths: parse() with various annotations, state transitions.
 * - Partition B: Boundary values: empty comments, missing braces, edge tokens.
 * - Partition C: Defect-targeted: @license and @preserve with embedded annotations (known bug).
 * - Partition D: Exception paths: invalid type syntax, missing closing tokens.
 * - Partition E: Object lifecycle: retrieveAndResetParsedJSDocInfo, hasParsedJSDocInfo.
 *
 * Known defect: testParseLicenseWithAnnotation fails because license text containing
 * an annotation (e.g., @author) is truncated. The parser stops at the annotation token
 * instead of preserving it as part of the license block.
 */
public class JsDocInfoParserDeepseekTest {

  // Helper to create a parser from a JSDoc comment string.
  private JsDocInfoParser createParser(String jsdocComment) {
    JsDocTokenStream stream = new JsDocTokenStream(jsdocComment);
    JSTypeRegistry registry = new JSTypeRegistry(NullErrorReporter.forOldRhino());
    // Build annotation names map (all standard annotations)
    Map<String, Annotation> annotationNames = new HashMap<>();
    for (Annotation ann : Annotation.values()) {
      annotationNames.put(ann.name().toLowerCase().replace('_', '-'), ann);
    }
    // Also add common aliases
    annotationNames.put("return", Annotation.RETURN);
    annotationNames.put("param", Annotation.PARAM);
    annotationNames.put("type", Annotation.TYPE);
    annotationNames.put("extends", Annotation.EXTENDS);
    annotationNames.put("implements", Annotation.IMPLEMENTS);
    annotationNames.put("throws", Annotation.THROWS);
    annotationNames.put("see", Annotation.SEE);
    annotationNames.put("template", Annotation.TEMPLATE);
    annotationNames.put("version", Annotation.VERSION);
    annotationNames.put("author", Annotation.AUTHOR);
    annotationNames.put("license", Annotation.LICENSE);
    annotationNames.put("preserve", Annotation.PRESERVE);
    annotationNames.put("suppress", Annotation.SUPPRESS);
    annotationNames.put("deprecated", Annotation.DEPRECATED);
    annotationNames.put("override", Annotation.OVERRIDE);
    annotationNames.put("constructor", Annotation.CONSTRUCTOR);
    annotationNames.put("interface", Annotation.INTERFACE);
    annotationNames.put("enum", Annotation.ENUM);
    annotationNames.put("this", Annotation.THIS);
    annotationNames.put("define", Annotation.DEFINE);
    annotationNames.put("typedef", Annotation.TYPEDEF);
    annotationNames.put("private", Annotation.PRIVATE);
    annotationNames.put("protected", Annotation.PROTECTED);
    annotationNames.put("public", Annotation.PUBLIC);
    annotationNames.put("const", Annotation.CONSTANT);
    annotationNames.put("export", Annotation.EXPORT);
    annotationNames.put("externs", Annotation.EXTERNS);
    annotationNames.put("hidden", Annotation.HIDDEN);
    annotationNames.put("noalias", Annotation.NO_ALIAS);
    annotationNames.put("noshadow", Annotation.NO_SHADOW);
    annotationNames.put("nosideeffects", Annotation.NO_SIDE_EFFECTS);
    annotationNames.put("implicitcast", Annotation.IMPLICIT_CAST);
    annotationNames.put("notypecheck", Annotation.NO_TYPE_CHECK);
    annotationNames.put("preservetry", Annotation.PRESERVE_TRY);
    annotationNames.put("javadispatch", Annotation.JAVA_DISPATCH);
    annotationNames.put("notimplemented", Annotation.NOT_IMPLEMENTED);
    annotationNames.put("inheritDoc", Annotation.INHERIT_DOC);
    annotationNames.put("override", Annotation.OVERRIDE);
    annotationNames.put("fileoverview", Annotation.FILE_OVERVIEW);
    annotationNames.put("desc", Annotation.DESC);

    Config config = new Config(registry, new HashSet<String>(), false);
    // We need to set annotationNames via reflection or assume Config has a setter?
    // Actually Config constructor takes a Map? Not shown. We'll use a workaround:
    // Since we cannot modify Config, we'll create a custom subclass? Not possible.
    // Instead, we'll rely on the fact that the parser uses annotationNames from config.
    // We'll assume Config has a public field or setter. For test purposes, we'll use
    // a helper that sets the annotationNames via reflection. But to keep it simple,
    // we'll assume the Config constructor accepts a Map. We'll create a Config with
    // a dummy registry and then set the annotationNames via a method.
    // Since we don't have the source, we'll use a workaround: create a Config using
    // the constructor that takes a JSTypeRegistry, Set<String>, and boolean.
    // Then we'll set the annotationNames field via reflection.
    try {
      java.lang.reflect.Field field = Config.class.getDeclaredField("annotationNames");
      field.setAccessible(true);
      field.set(config, annotationNames);
    } catch (Exception e) {
      throw new RuntimeException("Cannot set annotationNames", e);
    }

    ErrorReporter errorReporter = NullErrorReporter.forNewRhino();
    return new JsDocInfoParser(stream, "test", config, errorReporter);
  }

  // Helper to parse a comment and return JSDocInfo.
  private JSDocInfo parseComment(String comment) {
    JsDocInfoParser parser = createParser(comment);
    boolean success = parser.parse();
    if (!success) {
      return null;
    }
    return parser.retrieveAndResetParsedJSDocInfo();
  }

  // ==================== Partition A: Core Functional Logic ====================

  @Test(timeout = 4000)
  public void testSimpleAuthor() {
    JSDocInfo info = parseComment("/** @author John Doe */");
    assertNotNull(info);
    assertTrue(info.hasAuthor());
    assertEquals("John Doe", info.getAuthor());
  }

  @Test(timeout = 4000)
  public void testConstructor() {
    JSDocInfo info = parseComment("/** @constructor */");
    assertNotNull(info);
    assertTrue(info.isConstructor());
  }

  @Test(timeout = 4000)
  public void testInterface() {
    JSDocInfo info = parseComment("/** @interface */");
    assertNotNull(info);
    assertTrue(info.isInterface());
  }

  @Test(timeout = 4000)
  public void testDeprecatedWithReason() {
    JSDocInfo info = parseComment("/** @deprecated Use newMethod instead */");
    assertNotNull(info);
    assertTrue(info.isDeprecated());
    assertEquals("Use newMethod instead", info.getDeprecationReason());
  }

  @Test(timeout = 4000)
  public void testParamWithType() {
    JSDocInfo info = parseComment("/** @param {string} name The name */");
    assertNotNull(info);
    assertTrue(info.hasParameter("name"));
    JSTypeExpression type = info.getParameterType("name");
    assertNotNull(type);
    // We cannot easily assert the type string, but we can check it's not null.
  }

  @Test(timeout = 4000)
  public void testReturnType() {
    JSDocInfo info = parseComment("/** @return {number} The count */");
    assertNotNull(info);
    assertTrue(info.hasReturnType());
    assertNotNull(info.getReturnType());
  }

  @Test(timeout = 4000)
  public void testTypeAnnotation() {
    JSDocInfo info = parseComment("/** @type {string} */");
    assertNotNull(info);
    assertTrue(info.hasType());
    assertNotNull(info.getType());
  }

  @Test(timeout = 4000)
  public void testExtends() {
    JSDocInfo info = parseComment("/** @extends {BaseClass} */");
    assertNotNull(info);
    assertTrue(info.hasBaseType());
    assertNotNull(info.getBaseType());
  }

  @Test(timeout = 4000)
  public void testImplements() {
    JSDocInfo info = parseComment("/** @implements {Interface} */");
    assertNotNull(info);
    assertTrue(info.getImplementedInterfacesCount() > 0);
  }

  @Test(timeout = 4000)
  public void testEnum() {
    JSDocInfo info = parseComment("/** @enum {string} */");
    assertNotNull(info);
    assertTrue(info.hasEnumParameterType());
    assertNotNull(info.getEnumParameterType());
  }

  @Test(timeout = 4000)
  public void testVisibilityPrivate() {
    JSDocInfo info = parseComment("/** @private */");
    assertNotNull(info);
    assertEquals(JSDocInfo.Visibility.PRIVATE, info.getVisibility());
  }

  @Test(timeout = 4000)
  public void testVisibilityProtected() {
    JSDocInfo info = parseComment("/** @protected */");
    assertNotNull(info);
    assertEquals(JSDocInfo.Visibility.PROTECTED, info.getVisibility());
  }

  @Test(timeout = 4000)
  public void testVisibilityPublic() {
    JSDocInfo info = parseComment("/** @public */");
    assertNotNull(info);
    assertEquals(JSDocInfo.Visibility.PUBLIC, info.getVisibility());
  }

  @Test(timeout = 4000)
  public void testOverride() {
    JSDocInfo info = parseComment("/** @override */");
    assertNotNull(info);
    assertTrue(info.isOverride());
  }

  @Test(timeout = 4000)
  public void testSuppress() {
    JSDocInfo info = parseComment("/** @suppress {warning1|warning2} */");
    assertNotNull(info);
    Set<String> suppressions = info.getSuppressions();
    assertTrue(suppressions.contains("warning1"));
    assertTrue(suppressions.contains("warning2"));
  }

  @Test(timeout = 4000)
  public void testTemplate() {
    JSDocInfo info = parseComment("/** @template T */");
    assertNotNull(info);
    assertEquals("T", info.getTemplateTypeName());
  }

  @Test(timeout = 4000)
  public void testVersion() {
    JSDocInfo info = parseComment("/** @version 1.0 */");
    assertNotNull(info);
    assertEquals("1.0", info.getVersion());
  }

  @Test(timeout = 4000)
  public void testSee() {
    JSDocInfo info = parseComment("/** @see #myMethod */");
    assertNotNull(info);
    assertTrue(info.getReferences().contains("#myMethod"));
  }

  @Test(timeout = 4000)
  public void testFileOverview() {
    JSDocInfo info = parseComment("/** @fileoverview This is a file. */");
    assertNotNull(info);
    assertTrue(info.hasFileOverview());
    assertEquals("This is a file.", info.getFileOverview());
  }

  @Test(timeout = 4000)
  public void testDesc() {
    JSDocInfo info = parseComment("/** @desc Description text */");
    assertNotNull(info);
    assertTrue(info.isDescriptionRecorded());
    assertEquals("Description text", info.getDescription());
  }

  // ==================== Partition B: Boundary & Edge Cases ====================

  @Test(timeout = 4000)
  public void testEmptyComment() {
    JSDocInfo info = parseComment("/** */");
    assertNull(info); // parse returns true but build may return null if no annotations
  }

  @Test(timeout = 4000)
  public void testOnlyStars() {
    JSDocInfo info = parseComment("/** * */");
    assertNull(info);
  }

  @Test(timeout = 4000)
  public void testMissingClosingBrace() {
    // Type with missing '}'
    JSDocInfo info = parseComment("/** @type {string */");
    // Should still parse but with warning; info may be null or incomplete
    // We just ensure no crash.
    assertNotNull(info);
    // The type may be null because of error
    assertFalse(info.hasType());
  }

  @Test(timeout = 4000)
  public void testParamWithoutName() {
    JSDocInfo info = parseComment("/** @param {string} */");
    assertNotNull(info);
    // Should have no parameter recorded because name missing
    assertFalse(info.hasParameter(""));
  }

  @Test(timeout = 4000)
  public void testParamWithBracketsOptional() {
    JSDocInfo info = parseComment("/** @param {string=} [name] */");
    assertNotNull(info);
    assertTrue(info.hasParameter("name"));
    JSTypeExpression type = info.getParameterType("name");
    assertNotNull(type);
    // The type should be optional (wrapped with EQUALS)
  }

  @Test(timeout = 4000)
  public void testReturnWithDescription() {
    JSDocInfo info = parseComment("/** @return {number} The answer */");
    assertNotNull(info);
    assertTrue(info.hasReturnType());
    assertEquals("The answer", info.getReturnDescription());
  }

  @Test(timeout = 4000)
  public void testThrowsWithTypeAndDescription() {
    JSDocInfo info = parseComment("/** @throws {Error} If something fails */");
    assertNotNull(info);
    assertTrue(info.getThrownTypes().size() > 0);
    // Description is recorded
    JSTypeExpression thrownType = info.getThrownTypes().get(0);
    assertNotNull(thrownType);
    // We cannot easily check description mapping, but at least no crash.
  }

  // ==================== Partition C: Defect-Targeted (License/Preserve) ====================

  @Test(timeout = 4000)
  public void testParseLicenseWithAnnotation() {
    // This test targets the known defect: license text containing an annotation
    // should be preserved entirely.
    String comment = "/** @license Foo @author me */";
    JsDocInfoParser parser = createParser(comment);
    // We need to set fileLevelJsDocBuilder to capture the license text.
    // Since we don't have a real builder, we'll use a custom one.
    final StringBuilder captured = new StringBuilder();
    Node.FileLevelJsDocBuilder builder = new Node.FileLevelJsDocBuilder() {
      @Override
      public void append(String text) {
        captured.append(text);
      }
    };
    parser.setFileLevelJsDocBuilder(builder);
    boolean success = parser.parse();
    assertTrue("Parsing should succeed", success);
    // The license text should include the annotation
    assertEquals("License text should preserve annotation",
                 "Foo @author me", captured.toString().trim());
  }

  @Test(timeout = 4000)
  public void testParsePreserveWithAnnotation() {
    // Similar test for @preserve
    String comment = "/** @preserve Bar @type {string} */";
    JsDocInfoParser parser = createParser(comment);
    final StringBuilder captured = new StringBuilder();
    Node.FileLevelJsDocBuilder builder = new Node.FileLevelJsDocBuilder() {
      @Override
      public void append(String text) {
        captured.append(text);
      }
    };
    parser.setFileLevelJsDocBuilder(builder);
    boolean success = parser.parse();
    assertTrue("Parsing should succeed", success);
    assertEquals("Preserve text should preserve annotation",
                 "Bar @type {string}", captured.toString().trim());
  }

  @Test(timeout = 4000)
  public void testLicenseMultilineWithAnnotation() {
    // Multiline license with annotation on second line
    String comment = "/**\n * @license Line1\n * @author me\n */";
    JsDocInfoParser parser = createParser(comment);
    final StringBuilder captured = new StringBuilder();
    Node.FileLevelJsDocBuilder builder = new Node.FileLevelJsDocBuilder() {
      @Override
      public void append(String text) {
        captured.append(text);
      }
    };
    parser.setFileLevelJsDocBuilder(builder);
    boolean success = parser.parse();
    assertTrue("Parsing should succeed", success);
    String result = captured.toString().trim();
    assertTrue("Should contain both lines", result.contains("Line1"));
    assertTrue("Should contain annotation", result.contains("@author me"));
  }

  // ==================== Partition D: Exception & Defensive Guard Paths ====================

  @Test(timeout = 4000)
  public void testInvalidAnnotation() {
    // Unknown annotation should produce warning but not crash
    JSDocInfo info = parseComment("/** @unknownTag */");
    assertNotNull(info);
    // No specific info recorded
  }

  @Test(timeout = 4000)
  public void testTypeSyntaxError() {
    // Invalid type expression
    JSDocInfo info = parseComment("/** @type {*} */"); // * is valid
    assertNotNull(info);
    assertTrue(info.hasType());
    // Test invalid: missing closing brace
    info = parseComment("/** @type {string */");
    assertNotNull(info);
    assertFalse(info.hasType());
  }

  @Test(timeout = 4000)
  public void testDuplicateParam() {
    // Duplicate param should produce warning but still record first?
    JSDocInfo info = parseComment("/** @param {string} a @param {number} a */");
    assertNotNull(info);
    // The second param should overwrite? Actually it should warn and keep first?
    // We just ensure no crash.
    assertTrue(info.hasParameter("a"));
  }

  @Test(timeout = 4000)
  public void testIncompatibleType() {
    // @type after @constructor should warn
    JSDocInfo info = parseComment("/** @constructor @type {string} */");
    assertNotNull(info);
    // The @type should be ignored with warning
    assertFalse(info.hasType());
  }

  @Test(timeout = 4000)
  public void testEofInComment() {
    // Unclosed comment
    JsDocInfoParser parser = createParser("/** @param {string");
    boolean success = parser.parse();
    assertFalse("Should fail on EOF", success);
  }

  // ==================== Partition E: Object Lifecycle & Contract ====================

  @Test(timeout = 4000)
  public void testHasParsedJSDocInfo() {
    JsDocInfoParser parser = createParser("/** @type {number} */");
    assertFalse(parser.hasParsedJSDocInfo());
    parser.parse();
    assertTrue(parser.hasParsedJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testRetrieveAndReset() {
    JsDocInfoParser parser = createParser("/** @type {string} */");
    parser.parse();
    JSDocInfo info1 = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info1);
    assertTrue(info1.hasType());
    // After reset, hasParsedJSDocInfo should be false
    assertFalse(parser.hasParsedJSDocInfo());
    // Calling again should return null
    assertNull(parser.retrieveAndResetParsedJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testFileOverviewJSDocInfo() {
    JsDocInfoParser parser = createParser("/** @fileoverview Overview */");
    parser.parse();
    JSDocInfo fileOverview = parser.getFileOverviewJSDocInfo();
    assertNotNull(fileOverview);
    assertTrue(fileOverview.hasFileOverview());
    assertEquals("Overview", fileOverview.getFileOverview());
  }

  @Test(timeout = 4000)
  public void testMultipleFileOverviewWarning() {
    // Two fileoverview tags should produce warning but still record first?
    JsDocInfoParser parser = createParser("/** @fileoverview First */ /** @fileoverview Second */");
    // We need to parse two comments? Actually the parser only handles one comment.
    // We'll just parse the first.
    parser.parse();
    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertTrue(info.hasFileOverview());
    assertEquals("First", info.getFileOverview());
  }
}