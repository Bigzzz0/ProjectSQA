/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.parsing.JsDocInfoParser
 *
 * Defects4J Bug Target:
 * - Bug: com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLicenseWithAnnotation
 *   When extracting a multiline textual block for @license or @preserve with WhitespaceOption.PRESERVE,
 *   encountering an annotation like "@author" inside the license prematurely terminated extraction
 *   because case ANNOTATION was grouped with EOC and EOF without checking if whitespace was being preserved.
 *   Expected: "@license Foo \n * @author Bar\n */" should retain " Foo \n @author Bar" in file-level JSDoc.
 *
 * Branch & Coverage Matrix:
 * - Annotation parsing branches:
 *   - @author, @const, @constructor, @deprecated, @interface, @desc, @fileoverview,
 *     @license, @preserve, @enum, @export, @externs, @javadispatch, @extends, @implements,
 *     @hidden, @noalias, @nocheck, @override, @throws, @param (bracketed, optional, dots),
 *     @private, @protected, @public, @nosideeffects, @implicitcast, @see, @suppress,
 *     @template, @version, @define, @return, @this, @type, @typedef.
 * - Type syntax parsing:
 *   - parseTypeString (primitive, union, record, function, nullable, non-nullable, array, type application).
 *   - Function types with 'this:', varargs '...', return types.
 *   - Record types with field expressions.
 *   - Union types with pipe (|) and double pipes (||).
 * - Parser state transitions:
 *   - SEARCHING_ANNOTATION, SEARCHING_NEWLINE, NEXT_IS_ANNOTATION.
 *   - Comments with leading asterisks, empty lines, EOF/EOC transitions.
 *   - ErrorReporter warnings on duplicate or incompatible types.
 */

package com.google.javascript.jscomp.parsing;

import com.google.common.collect.Sets;
import com.google.javascript.jscomp.mozilla.rhino.ErrorReporter;
import com.google.javascript.jscomp.mozilla.rhino.EvaluatorException;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfo.Visibility;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class JsDocInfoParserGeminiTest {

  private static class TestErrorReporter implements ErrorReporter {
    final List<String> warnings = new ArrayList<String>();
    final List<String> errors = new ArrayList<String>();

    @Override
    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
      warnings.add(message);
    }

    @Override
    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
      errors.add(message);
    }

    @Override
    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
      return new EvaluatorException(message);
    }
  }

  private JsDocInfoParser createParser(String comment, boolean parseDocumentation, TestErrorReporter errorReporter) {
    Config config = new Config(
        new JSTypeRegistry(NullErrorReporter.forOldRhino()),
        Sets.<String>newHashSet(),
        parseDocumentation);
    JsDocTokenStream stream = new JsDocTokenStream(comment);
    return new JsDocInfoParser(stream, "testcode", config, errorReporter);
  }

  private JSDocInfo parse(String comment) {
    return parse(comment, false, new TestErrorReporter());
  }

  private JSDocInfo parse(String comment, boolean parseDocumentation) {
    return parse(comment, parseDocumentation, new TestErrorReporter());
  }

  private JSDocInfo parse(String comment, boolean parseDocumentation, TestErrorReporter reporter) {
    JsDocInfoParser parser = createParser(comment, parseDocumentation, reporter);
    parser.parse();
    return parser.retrieveAndResetParsedJSDocInfo();
  }

  // =========================================================================
  // Partition C: Defect-Targeted Zone (Defects4J License Parsing Defect)
  // =========================================================================

  /**
   * Targets the defect where multiline @license blocks prematurely terminate
   * when encountering an embedded annotation token like @author.
   */
  @Test(timeout = 4000)
  public void testParseLicenseWithAnnotation() {
    String comment = "@license Foo \n * @author Bar\n */";
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfoParser parser = createParser(comment, false, reporter);

    Node scriptNode = new Node(Token.SCRIPT);
    Node.FileLevelJsDocBuilder fileLevelBuilder = scriptNode.getJsDocBuilderForNode();
    parser.setFileLevelJsDocBuilder(fileLevelBuilder);

    assertTrue(parser.parse());
    assertNull(parser.retrieveAndResetParsedJSDocInfo());
    assertEquals(" Foo \n @author Bar", fileLevelBuilder.getJsDoc());
  }

  @Test(timeout = 4000)
  public void testParsePreserveBlockPreservesFormatting() {
    String comment = "@preserve First line\n * Second line\n */";
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfoParser parser = createParser(comment, false, reporter);

    Node scriptNode = new Node(Token.SCRIPT);
    Node.FileLevelJsDocBuilder fileLevelBuilder = scriptNode.getJsDocBuilderForNode();
    parser.setFileLevelJsDocBuilder(fileLevelBuilder);

    assertTrue(parser.parse());
    assertEquals(" First line\n Second line", fileLevelBuilder.getJsDoc());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testParseBasicAnnotations() {
    JSDocInfo info = parse(
        "@constructor\n" +
        " * @export\n" +
        " * @noalias\n" +
        " * @nocheck\n" +
        " * @override\n" +
        " * @nosideeffects\n" +
        " */");

    assertNotNull(info);
    assertTrue(info.isConstructor());
    assertTrue(info.isExport());
    assertTrue(info.isNoAlias());
    assertTrue(info.isNoTypeCheck());
    assertTrue(info.isOverride());
    assertTrue(info.isNoSideEffects());
  }

  @Test(timeout = 4000)
  public void testParseVisibilities() {
    JSDocInfo privateInfo = parse("@private\n */");
    assertNotNull(privateInfo);
    assertEquals(Visibility.PRIVATE, privateInfo.getVisibility());

    JSDocInfo protectedInfo = parse("@protected\n */");
    assertNotNull(protectedInfo);
    assertEquals(Visibility.PROTECTED, protectedInfo.getVisibility());

    JSDocInfo publicInfo = parse("@public\n */");
    assertNotNull(publicInfo);
    assertEquals(Visibility.PUBLIC, publicInfo.getVisibility());
  }

  @Test(timeout = 4000)
  public void testParseAuthorAndVersion() {
    JSDocInfo info = parse(
        "@author Jane Doe\n" +
        " * @version 1.2.3\n" +
        " */");
    assertNotNull(info);
    assertTrue(info.getAuthors().contains("Jane Doe"));
    assertEquals("1.2.3", info.getVersion());
  }

  @Test(timeout = 4000)
  public void testParseConstantAndDeprecated() {
    JSDocInfo info = parse(
        "@const\n" +
        " * @deprecated Obsolete function.\n" +
        " */");
    assertNotNull(info);
    assertTrue(info.isConstant());
    assertTrue(info.isDeprecated());
    assertEquals("Obsolete function.", info.getDeprecationReason());
  }

  @Test(timeout = 4000)
  public void testParseInterfaceAndExtends() {
    JSDocInfo info = parse(
        "@interface\n" +
        " * @extends {BaseInterface}\n" +
        " */");
    assertNotNull(info);
    assertTrue(info.isInterface());
    assertEquals(1, info.getBaseTypeCount());
  }

  @Test(timeout = 4000)
  public void testParseImplementsMultiple() {
    JSDocInfo info = parse(
        "@constructor\n" +
        " * @implements {InterfaceA}\n" +
        " * @implements {InterfaceB}\n" +
        " */");
    assertNotNull(info);
    assertEquals(2, info.getImplementedInterfaceCount());
  }

  @Test(timeout = 4000)
  public void testParseDescriptionAndBlockComment() {
    JSDocInfo info = parse(
        "Top block comment\n" +
        " * description.\n" +
        " * @desc Multiline\n" +
        " * description text.\n" +
        " */", true);

    assertNotNull(info);
    assertEquals("Top block comment\ndescription.", info.getBlockDescription());
    assertEquals("Multiline description text.", info.getDescription());
  }

  @Test(timeout = 4000)
  public void testParseParamsStandardAndOptional() {
    JSDocInfo info = parse(
        "@param {string} a Regular param\n" +
        " * @param {number=} opt_b Optional param\n" +
        " * @param {[c]} Bracketed param\n" +
        " */", true);

    assertNotNull(info);
    assertTrue(info.hasParameter("a"));
    assertTrue(info.hasParameter("opt_b"));
    assertTrue(info.hasParameter("c"));
    assertEquals("Regular param", info.getParameterDescription("a"));
    assertEquals("Optional param", info.getParameterDescription("opt_b"));
  }

  @Test(timeout = 4000)
  public void testParseParamWithDefaultToolkitSyntax() {
    JSDocInfo info = parse("@param {string} [foo=defaultVal] Optional\n */");
    assertNotNull(info);
    assertTrue(info.hasParameter("foo"));
  }

  @Test(timeout = 4000)
  public void testParseReturnsAndThrows() {
    JSDocInfo info = parse(
        "@return {boolean} Success indicator\n" +
        " * @throws {Error} Failure reason\n" +
        " */", true);

    assertNotNull(info);
    assertNotNull(info.getReturnType());
    assertEquals("Success indicator", info.getReturnDescription());
    assertEquals(1, info.getThrownTypes().size());
    assertEquals("Failure reason", info.getThrowsDescriptionForType(info.getThrownTypes().get(0)));
  }

  @Test(timeout = 4000)
  public void testParseEnum() {
    JSDocInfo info = parse("@enum {number}\n */");
    assertNotNull(info);
    assertNotNull(info.getEnumParameterType());
  }

  @Test(timeout = 4000)
  public void testParseEnumDefaultType() {
    JSDocInfo info = parse("@enum\n */");
    assertNotNull(info);
    assertNotNull(info.getEnumParameterType());
  }

  @Test(timeout = 4000)
  public void testParseSuppressTag() {
    JSDocInfo info = parse("@suppress {visibility|checkTypes}\n */");
    assertNotNull(info);
    assertEquals(2, info.getSuppressions().size());
    assertTrue(info.getSuppressions().contains("visibility"));
    assertTrue(info.getSuppressions().contains("checkTypes"));
  }

  @Test(timeout = 4000)
  public void testParseTemplateAndSee() {
    JSDocInfo info = parse(
        "@template T\n" +
        " * @see http://example.com\n" +
        " */");
    assertNotNull(info);
    assertEquals("T", info.getTemplateTypeName());
    assertTrue(info.getReferences().contains("http://example.com"));
  }

  @Test(timeout = 4000)
  public void testParseFileOverview() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfoParser parser = createParser("@fileoverview Sample file overview.\n */", true, reporter);
    assertTrue(parser.parse());
    JSDocInfo fileDoc = parser.getFileOverviewJSDocInfo();
    assertNotNull(fileDoc);
    assertEquals("Sample file overview.", fileDoc.getFileOverview());
  }

  @Test(timeout = 4000)
  public void testParseTypedefAndDefineAndThis() {
    JSDocInfo info = parse(
        "@typedef {string|number}\n" +
        " * @this {Object}\n" +
        " */");
    assertNotNull(info);
    assertNotNull(info.getTypedefType());
    assertNotNull(info.getThisType());

    JSDocInfo defineInfo = parse("@define {boolean}\n */");
    assertNotNull(defineInfo);
    assertNotNull(defineInfo.getType());
  }

  @Test(timeout = 4000)
  public void testParseMiscellaneousFlags() {
    JSDocInfo info = parse(
        "@externs\n" +
        " * @javadispatch\n" +
        " * @hidden\n" +
        " * @noshadow\n" +
        " * @implicitcast\n" +
        " * @preserveTry\n" +
        " */");
    assertNotNull(info);
    assertTrue(info.isExterns());
    assertTrue(info.isJavaDispatch());
    assertTrue(info.isHidden());
    assertTrue(info.isNoShadow());
    assertTrue(info.isImplicitCast());
    assertTrue(info.shouldPreserveTry());
  }

  // =========================================================================
  // Partition B: Type String Parsing & Grammar Boundaries
  // =========================================================================

  @Test(timeout = 4000)
  public void testParseTypeStringPrimitives() {
    Node nullNode = JsDocInfoParser.parseTypeString("null");
    assertNotNull(nullNode);
    assertEquals(Token.STRING, nullNode.getType());
    assertEquals("null", nullNode.getString());

    Node undefNode = JsDocInfoParser.parseTypeString("undefined");
    assertNotNull(undefNode);
    assertEquals("undefined", undefNode.getString());

    Node allNode = JsDocInfoParser.parseTypeString("*");
    assertNotNull(allNode);
    assertEquals(Token.STAR, allNode.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringModifiers() {
    Node nullable = JsDocInfoParser.parseTypeString("?string");
    assertNotNull(nullable);
    assertEquals(Token.QMARK, nullable.getType());

    Node nonNullable = JsDocInfoParser.parseTypeString("!number");
    assertNotNull(nonNullable);
    assertEquals(Token.BANG, nonNullable.getType());

    Node postNullable = JsDocInfoParser.parseTypeString("boolean?");
    assertNotNull(postNullable);
    assertEquals(Token.QMARK, postNullable.getType());

    Node postNonNullable = JsDocInfoParser.parseTypeString("boolean!");
    assertNotNull(postNonNullable);
    assertEquals(Token.BANG, postNonNullable.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringUnion() {
    Node union = JsDocInfoParser.parseTypeString("(number|string|boolean)");
    assertNotNull(union);
    assertEquals(Token.PIPE, union.getType());
    assertEquals(3, union.getChildCount());

    Node topLevelUnion = JsDocInfoParser.parseTypeString("number|string");
    assertNotNull(topLevelUnion);
    assertEquals(Token.PIPE, topLevelUnion.getType());

    Node doublePipeUnion = JsDocInfoParser.parseTypeString("number||string");
    assertNotNull(doublePipeUnion);
    assertEquals(Token.PIPE, doublePipeUnion.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringArray() {
    Node array = JsDocInfoParser.parseTypeString("[number, string]");
    assertNotNull(array);
    assertEquals(Token.LB, array.getType());
    assertEquals(2, array.getChildCount());

    Node arrayVarArgs = JsDocInfoParser.parseTypeString("[number, ...string]");
    assertNotNull(arrayVarArgs);
    assertEquals(Token.LB, arrayVarArgs.getType());
    assertEquals(2, arrayVarArgs.getChildCount());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringRecord() {
    Node record = JsDocInfoParser.parseTypeString("{foo: string, bar: number}");
    assertNotNull(record);
    assertEquals(Token.LC, record.getType());
    Node fieldList = record.getFirstChild();
    assertEquals(Token.LB, fieldList.getType());
    assertEquals(2, fieldList.getChildCount());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringTypeApplication() {
    Node app = JsDocInfoParser.parseTypeString("Array.<string, number>");
    assertNotNull(app);
    assertEquals(Token.STRING, app.getType());
    assertEquals("Array", app.getString());
    Node params = app.getFirstChild();
    assertEquals(Token.BLOCK, params.getType());
    assertEquals(2, params.getChildCount());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringFunctionSignature() {
    Node fn = JsDocInfoParser.parseTypeString("function(this:Object, string, number=): void");
    assertNotNull(fn);
    assertEquals(Token.FUNCTION, fn.getType());
    assertEquals(3, fn.getChildCount()); // this, params, return

    Node thisNode = fn.getFirstChild();
    assertEquals(Token.THIS, thisNode.getType());

    Node paramsNode = thisNode.getNext();
    assertEquals(Token.LP, paramsNode.getType());

    Node returnNode = fn.getLastChild();
    assertEquals(Token.VOID, returnNode.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringFunctionWithVarArgs() {
    Node fn = JsDocInfoParser.parseTypeString("function(...[number]): boolean");
    assertNotNull(fn);
    assertEquals(Token.FUNCTION, fn.getType());

    Node params = fn.getFirstChild();
    assertEquals(Token.LP, params.getType());
    assertEquals(1, params.getChildCount());
    assertEquals(Token.ELLIPSIS, params.getFirstChild().getType());
  }

  // =========================================================================
  // Partition D: Error Handling, Warnings, and Edge Cases
  // =========================================================================

  @Test(timeout = 4000)
  public void testUnknownAnnotationWarning() {
    TestErrorReporter reporter = new TestErrorReporter();
    parse("@unknownAnnotation\n */", false, reporter);
    assertFalse(reporter.warnings.isEmpty());
  }

  @Test(timeout = 4000)
  public void testIncompatibleConstructorAndInterface() {
    TestErrorReporter reporter = new TestErrorReporter();
    parse("@constructor\n * @interface\n */", false, reporter);
    assertFalse(reporter.warnings.isEmpty());
  }

  @Test(timeout = 4000)
  public void testDuplicateParameterWarning() {
    TestErrorReporter reporter = new TestErrorReporter();
    parse("@param {number} x\n * @param {string} x\n */", false, reporter);
    assertFalse(reporter.warnings.isEmpty());
  }

  @Test(timeout = 4000)
  public void testMissingParameterNameWarning() {
    TestErrorReporter reporter = new TestErrorReporter();
    parse("@param {number}\n */", false, reporter);
    assertFalse(reporter.warnings.isEmpty());
  }

  @Test(timeout = 4000)
  public void testParamWithPropertySyntaxIgnored() {
    JSDocInfo info = parse("@param {string} user.name\n */");
    assertNull(info); // property params are discarded quietly
  }

  @Test(timeout = 4000)
  public void testMissingAuthorWarning() {
    TestErrorReporter reporter = new TestErrorReporter();
    parse("@author\n */", false, reporter);
    assertFalse(reporter.warnings.isEmpty());
  }

  @Test(timeout = 4000)
  public void testMissingSeeWarning() {
    TestErrorReporter reporter = new TestErrorReporter();
    parse("@see\n */", false, reporter);
    assertFalse(reporter.warnings.isEmpty());
  }

  @Test(timeout = 4000)
  public void testMissingVersionWarning() {
    TestErrorReporter reporter = new TestErrorReporter();
    parse("@version\n */", false, reporter);
    assertFalse(reporter.warnings.isEmpty());
  }

  @Test(timeout = 4000)
  public void testMissingTemplateTypeNameWarning() {
    TestErrorReporter reporter = new TestErrorReporter();
    parse("@template\n */", false, reporter);
    assertFalse(reporter.warnings.isEmpty());
  }

  @Test(timeout = 4000)
  public void testDuplicateFileOverviewWarning() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfoParser parser = createParser("@fileoverview First.\n * @fileoverview Second.\n */", false, reporter);
    parser.parse();
    assertFalse(reporter.warnings.isEmpty());
  }

  @Test(timeout = 4000)
  public void testDuplicateSuppressionsWarning() {
    TestErrorReporter reporter = new TestErrorReporter();
    parse("@suppress {visibility}\n * @suppress {visibility}\n */", false, reporter);
    assertFalse(reporter.warnings.isEmpty());
  }

  @Test(timeout = 4000)
  public void testInvalidDefineType() {
    TestErrorReporter reporter = new TestErrorReporter();
    parse("@define {Object}\n */", false, reporter);
    assertFalse(reporter.warnings.isEmpty());
  }

  @Test(timeout = 4000)
  public void testInvalidTypeSyntaxReturnsNull() {
    assertNull(JsDocInfoParser.parseTypeString(""));
    assertNull(JsDocInfoParser.parseTypeString("Array.<"));
    assertNull(JsDocInfoParser.parseTypeString("{foo: }"));
    assertNull(JsDocInfoParser.parseTypeString("function(:void"));
  }

  @Test(timeout = 4000)
  public void testMissingBracesWarningInExtends() {
    TestErrorReporter reporter = new TestErrorReporter();
    parse("@extends {Base\n */", false, reporter);
    assertFalse(reporter.warnings.isEmpty());
  }

  @Test(timeout = 4000)
  public void testBracelessTypeExtends() {
    JSDocInfo info = parse("@extends BaseClass\n */");
    assertNotNull(info);
    assertEquals(1, info.getBaseTypeCount());
  }

  @Test(timeout = 4000)
  public void testUnexpectedEOFHandling() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfoParser parser = createParser("@constructor", false, reporter);
    assertFalse(parser.parse());
    assertFalse(reporter.warnings.isEmpty());
  }

  @Test(timeout = 4000)
  public void testEmptyCommentParsing() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfoParser parser = createParser("*/", false, reporter);
    assertTrue(parser.parse());
    assertFalse(parser.hasParsedJSDocInfo());
  }
}