package com.google.javascript.jscomp.parsing;

import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfo.Visibility;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.EvaluatorException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.parsing.JsDocInfoParser
 *
 * Defect-Targeted Branches (Closure Defects4J Ground Truth):
 * - IntegrationTest::testIssue701, JsDocInfoParserTest::testParseLicense,
 *   testParsePreserve, testParseLicenseAscii:
 *   --> extractMultilineTextualBlock with WhitespaceOption.PRESERVE improperly handled
 *       whitespace preservation and token spacing, failing to preserve leading whitespace
 *       and multiline indentations on @license and @preserve tags.
 *
 * Equivalence Partitions & Decision Coverage:
 * - Partition A: Core Functional Logic & State Transitions:
 *   * All major JSDoc tags: @author, @const, @constructor, @deprecated, @desc, @enum,
 *     @export, @expose, @extends, @implements, @interface, @lends, @meaning, @modifies,
 *     @param, @private, @protected, @public, @return, @see, @suppress, @template,
 *     @this, @throws, @type, @typedef, @version.
 *   * Documentation preservation toggles: shouldParseDocumentation true/false.
 * - Partition B: Boundary Value Analysis (BVA) & Extremes:
 *   * Empty comments, stars-only comments, unexpected EOF, missing curly braces,
 *     missing bracket pairs, missing type names.
 * - Partition C: Defect-Targeted Branch Zone:
 *   * @license and @preserve multiline extraction with leading spaces, newline preservation,
 *     and ASCII-art comment formatting.
 * - Partition D: Exception & Defensive Guard Paths:
 *   * Unknown tags, duplicate incompatible annotations, duplicate type declarations,
 *     syntax errors during type descent, unrecognized modifier targets.
 * - Partition E: Object Lifecycle & Parser Contract Integrity:
 *   * parseTypeString standalone type expressions (record, union, function, array,
 *     nullable, bang-wrapped, varargs).
 */
public class JsDocInfoParserGeminiTest {

  private final List<String> warnings = new ArrayList<String>();

  private ErrorReporter createErrorReporter() {
    return new ErrorReporter() {
      @Override
      public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
        warnings.add(message);
      }

      @Override
      public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
        warnings.add("ERROR: " + message);
      }

      @Override
      public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
        warnings.add("RUNTIME: " + message);
        return new EvaluatorException(message);
      }
    };
  }

  private Config createConfig(boolean parseDoc) {
    Set<String> extraAnnotations = new HashSet<String>();
    Set<String> suppressions = new HashSet<String>();
    suppressions.add("visibility");
    suppressions.add("accessControls");
    suppressions.add("checkTypes");
    return new Config(extraAnnotations, suppressions, parseDoc, LanguageMode.ECMASCRIPT3, false);
  }

  private JsDocInfoParser createParser(String comment, boolean parseDoc, Node associatedNode) {
    warnings.clear();
    Config config = createConfig(parseDoc);
    JsDocTokenStream stream = new JsDocTokenStream(comment);
    return new JsDocInfoParser(stream, null, associatedNode, config, createErrorReporter());
  }

  private boolean hasWarning(String sub) {
    for (String w : warnings) {
      if (w.contains(sub)) {
        return true;
      }
    }
    return false;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure @license / @preserve bugs)
  // =========================================================================

  @Test(timeout = 4000)
  public void testParseLicenseDirectDefectTrigger() {
    Node scriptNode = IR.script();
    Node.FileLevelJsDocBuilder builder = scriptNode.getJsDocBuilder();
    JsDocInfoParser parser = createParser("@license Foo\nBar\n   Baz*/", true, scriptNode);
    parser.setFileLevelJsDocBuilder(builder);

    assertTrue(parser.parse());
    assertNull(parser.retrieveAndResetParsedJSDocInfo());
    assertEquals(" Foo\nBar\n   Baz", scriptNode.getJsDoc());
  }

  @Test(timeout = 4000)
  public void testParsePreserveDirectDefectTrigger() {
    Node scriptNode = IR.script();
    Node.FileLevelJsDocBuilder builder = scriptNode.getJsDocBuilder();
    JsDocInfoParser parser = createParser("@preserve Foo\nBar\n   Baz*/", true, scriptNode);
    parser.setFileLevelJsDocBuilder(builder);

    assertTrue(parser.parse());
    assertNull(parser.retrieveAndResetParsedJSDocInfo());
    assertEquals(" Foo\nBar\n   Baz", scriptNode.getJsDoc());
  }

  @Test(timeout = 4000)
  public void testParseLicenseAsciiDirectDefectTrigger() {
    Node scriptNode = IR.script();
    Node.FileLevelJsDocBuilder builder = scriptNode.getJsDocBuilder();
    JsDocInfoParser parser = createParser("@license Foo\n *  Bar\n *   Baz\n */", true, scriptNode);
    parser.setFileLevelJsDocBuilder(builder);

    assertTrue(parser.parse());
    assertEquals(" Foo\n  Bar\n   Baz\n", scriptNode.getJsDoc());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testParseConstructorAndVisibility() {
    JsDocInfoParser parser = createParser("@constructor\n@private*/", false, null);
    assertTrue(parser.parse());
    assertTrue(parser.hasParsedJSDocInfo());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isConstructor());
    assertEquals(Visibility.PRIVATE, info.getVisibility());
    assertFalse(parser.hasParsedJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testParseInterfaceAndExtends() {
    JsDocInfoParser parser = createParser("@interface\n@extends {SuperInterface}\n@extends {SuperInterface2}*/", false, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isInterface());
    assertEquals(2, info.getExtendedInterfacesCount());
  }

  @Test(timeout = 4000)
  public void testParseClassExtendsAndImplements() {
    JsDocInfoParser parser = createParser("@constructor\n@extends SuperClass\n@implements {Interface1}*/", false, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isConstructor());
    assertNotNull(info.getBaseType());
    assertEquals(1, info.getImplementedInterfaceCount());
  }

  @Test(timeout = 4000)
  public void testParseParamWithTypesAndDescription() {
    JsDocInfoParser parser = createParser("@param {number} x The x coordinate\n@param {string=} opt_y Optional y*/", true, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertEquals(2, info.getParameterCount());
    assertTrue(info.hasParameterType("x"));
    assertTrue(info.hasParameterType("opt_y"));
    assertEquals("The x coordinate", info.getParameterDescription("x"));
    assertEquals("Optional y", info.getParameterDescription("opt_y"));
  }

  @Test(timeout = 4000)
  public void testParseBracketedOptionalParamWithDefault() {
    JsDocInfoParser parser = createParser("@param {string} [name='defaultVal'] The name*/", true, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertTrue(info.hasParameterType("name"));
    assertEquals("The name", info.getParameterDescription("name"));
  }

  @Test(timeout = 4000)
  public void testParseReturnWithTypeAndDescription() {
    JsDocInfoParser parser = createParser("@return {boolean} True on success*/", true, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertNotNull(info.getReturnType());
    assertEquals("True on success", info.getReturnDescription());
  }

  @Test(timeout = 4000)
  public void testParseReturnWithoutTypeInfersUnknown() {
    JsDocInfoParser parser = createParser("@return Success indicator*/", true, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertNotNull(info.getReturnType());
    assertEquals("Success indicator", info.getReturnDescription());
  }

  @Test(timeout = 4000)
  public void testParseEnumDefaultsToNumber() {
    JsDocInfoParser parser = createParser("@enum*/", false, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertNotNull(info.getEnumParameterType());
  }

  @Test(timeout = 4000)
  public void testParseEnumExplicitType() {
    JsDocInfoParser parser = createParser("@enum {string}*/", false, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertNotNull(info.getEnumParameterType());
  }

  @Test(timeout = 4000)
  public void testParseFileOverview() {
    JsDocInfoParser parser = createParser("@fileoverview Sample file overview description\nmultiline content*/", true, null);
    assertTrue(parser.parse());

    JSDocInfo overview = parser.getFileOverviewJSDocInfo();
    assertNotNull(overview);
    assertTrue(overview.getFileOverview().contains("Sample file overview description"));
  }

  @Test(timeout = 4000)
  public void testParseSuppressTag() {
    JsDocInfoParser parser = createParser("@suppress {visibility|accessControls}*/", false, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    Set<String> suppressions = info.getSuppressions();
    assertEquals(2, suppressions.size());
    assertTrue(suppressions.contains("visibility"));
    assertTrue(suppressions.contains("accessControls"));
  }

  @Test(timeout = 4000)
  public void testParseModifiesTag() {
    JsDocInfoParser parser = createParser("@param {Object} x\n@modifies {this|arguments|x}*/", false, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    Set<String> modifies = info.getModifies();
    assertEquals(3, modifies.size());
    assertTrue(modifies.contains("this"));
    assertTrue(modifies.contains("arguments"));
    assertTrue(modifies.contains("x"));
  }

  @Test(timeout = 4000)
  public void testParseAllSimpleFlagAnnotations() {
    String comment =
        "@const\n" +
        "@consistentIdGenerator\n" +
        "@export\n" +
        "@expose\n" +
        "@externs\n" +
        "@idGenerator\n" +
        "@implicitCast\n" +
        "@javadispatch\n" +
        "@noalias\n" +
        "@nocompile\n" +
        "@nosideeffects\n" +
        "@noshadow\n" +
        "@notimplemented\n" +
        "@override\n" +
        "@preserveTry\n" +
        "@hidden*/";

    JsDocInfoParser parser = createParser(comment, false, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isConstant());
    assertTrue(info.isConsistentIdGenerator());
    assertTrue(info.isExport());
    assertTrue(info.isExpose());
    assertTrue(info.isIdGenerator());
    assertTrue(info.isImplicitCast());
    assertTrue(info.isJavaDispatch());
    assertTrue(info.isNoAlias());
    assertTrue(info.isNoCompile());
    assertTrue(info.isNoSideEffects());
    assertTrue(info.isNoShadow());
    assertTrue(info.isOverride());
    assertTrue(info.shouldPreserveTry());
    assertTrue(info.isHidden());
  }

  @Test(timeout = 4000)
  public void testParseTextualAnnotations() {
    String comment =
        "@author John Doe\n" +
        "@desc Sample description\n" +
        "@deprecated Use replacement\n" +
        "@meaning Internal meaning\n" +
        "@see http://example.com\n" +
        "@template T\n" +
        "@version 1.2.3*/";

    JsDocInfoParser parser = createParser(comment, true, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertTrue(info.getAuthors().contains("John Doe"));
    assertEquals("Sample description", info.getDescription());
    assertEquals("Use replacement", info.getDeprecationReason());
    assertEquals("Internal meaning", info.getMeaning());
    assertTrue(info.getReferences().contains("http://example.com"));
    assertEquals(1, info.getTemplateTypeNames().size());
    assertTrue(info.getTemplateTypeNames().contains("T"));
    assertEquals("1.2.3", info.getVersion());
  }

  @Test(timeout = 4000)
  public void testParseThisAndTypedefAndTypeAndDefine() {
    JsDocInfoParser parser = createParser("@this {Object}\n@typedef {string}\n@type {number}\n@define {boolean}*/", false, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertNotNull(info.getThisType());
    assertNotNull(info.getTypedefType());
    assertNotNull(info.getType());
    assertNotNull(info.getBaseType()); // define type
  }

  @Test(timeout = 4000)
  public void testParseLends() {
    JsDocInfoParser parser = createParser("@lends {Namespace.Target}*/", false, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertEquals("Namespace.Target", info.getLendsName());
  }

  @Test(timeout = 4000)
  public void testParseThrows() {
    JsDocInfoParser parser = createParser("@throws {Error} When failed*/", true, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertEquals(1, info.getThrownTypes().size());
  }

  @Test(timeout = 4000)
  public void testParseTopLevelBlockComment() {
    JsDocInfoParser parser = createParser("Top level block comment\n * with star lines\n@constructor*/", true, null);
    assertTrue(parser.parse());

    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertTrue(info.getBlockDescription().contains("Top level block comment"));
    assertTrue(info.isConstructor());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testParseEmptyComment() {
    JsDocInfoParser parser = createParser("*/", false, null);
    assertTrue(parser.parse());
    assertFalse(parser.hasParsedJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testParseOnlyStarsComment() {
    JsDocInfoParser parser = createParser("*****\n * ***\n */", false, null);
    assertTrue(parser.parse());
    assertFalse(parser.hasParsedJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testParseUnexpectedEofReturnsFalse() {
    JsDocInfoParser parser = createParser("@param", false, null);
    boolean parsed = parser.parse();
    assertFalse(parsed);
    assertTrue(hasWarning("Unexpected end of file"));
  }

  @Test(timeout = 4000)
  public void testParseEmptyTypeString() {
    Node node = JsDocInfoParser.parseTypeString("");
    assertNull(node);
  }

  @Test(timeout = 4000)
  public void testParseMissingVariableInParam() {
    JsDocInfoParser parser = createParser("@param {number}*/", false, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("variable name"));
  }

  @Test(timeout = 4000)
  public void testParseDotInParamNameSilentlyIgnored() {
    JsDocInfoParser parser = createParser("@param {number} options.timeout */", false, null);
    assertTrue(parser.parse());
    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertEquals(0, info.getParameterCount());
  }

  @Test(timeout = 4000)
  public void testParseMissingBracketInBracketedParam() {
    JsDocInfoParser parser = createParser("@param {number} [opt_x */", false, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("missing ']'"));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testUnknownBadJsDocTagWarning() {
    JsDocInfoParser parser = createParser("@nonExistentTag*/", false, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("nonExistentTag"));
  }

  @Test(timeout = 4000)
  public void testDuplicateConstructorWarning() {
    JsDocInfoParser parser = createParser("@constructor\n@constructor*/", false, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("incompat.type") || hasWarning("type cannot"));
  }

  @Test(timeout = 4000)
  public void testConstructorAndInterfaceConflict() {
    JsDocInfoParser parser = createParser("@constructor\n@interface*/", false, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("cannot be both or an interface and a constructor") || hasWarning("interface.constructor"));
  }

  @Test(timeout = 4000)
  public void testDuplicateParamWarning() {
    JsDocInfoParser parser = createParser("@param {number} x\n@param {string} x*/", false, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("duplicate") || hasWarning("dup.variable.name"));
  }

  @Test(timeout = 4000)
  public void testDuplicateFileOverviewWarning() {
    JsDocInfoParser parser = createParser("@fileoverview first\n@fileoverview second*/", true, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("fileoverview.extra") || hasWarning("more than once"));
  }

  @Test(timeout = 4000)
  public void testFileOverviewAlreadyRegisteredWarning() {
    JsDocInfoParser parser = createParser("@fileoverview test*/", true, null);
    parser.setFileOverviewJSDocInfo(new JSDocInfo());
    assertTrue(parser.parse());
    assertTrue(hasWarning("fileoverview.extra") || hasWarning("more than once"));
  }

  @Test(timeout = 4000)
  public void testDuplicateExtendsOnClassWarning() {
    JsDocInfoParser parser = createParser("@constructor\n@extends Base1\n@extends Base2*/", false, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("incompat.type") || hasWarning("cannot extend more than one"));
  }

  @Test(timeout = 4000)
  public void testDuplicateExtendsOnInterfaceWarning() {
    JsDocInfoParser parser = createParser("@interface\n@extends {Iface}\n@extends {Iface}*/", false, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("extends.duplicate") || hasWarning("more than once"));
  }

  @Test(timeout = 4000)
  public void testDuplicateImplementsWarning() {
    JsDocInfoParser parser = createParser("@implements {Iface}\n@implements {Iface}*/", false, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("implements.duplicate") || hasWarning("more than once"));
  }

  @Test(timeout = 4000)
  public void testUnknownSuppressionWarning() {
    JsDocInfoParser parser = createParser("@suppress {unknownWarningName}*/", false, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("unknownWarningName"));
  }

  @Test(timeout = 4000)
  public void testUnknownModifiesTargetWarning() {
    JsDocInfoParser parser = createParser("@modifies {unknownTarget}*/", false, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("unknownTarget"));
  }

  @Test(timeout = 4000)
  public void testExtendsMissingTypeNameWarning() {
    JsDocInfoParser parser = createParser("@extends */", false, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("no.type.name") || hasWarning("type name"));
  }

  @Test(timeout = 4000)
  public void testLendsMissingTargetWarning() {
    JsDocInfoParser parser = createParser("@lends */", false, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("lends.missing") || hasWarning("Missing"));
  }

  @Test(timeout = 4000)
  public void testEmptyAuthorWarning() {
    JsDocInfoParser parser = createParser("@author \n*/", true, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("authormissing") || hasWarning("author"));
  }

  @Test(timeout = 4000)
  public void testEmptySeeWarning() {
    JsDocInfoParser parser = createParser("@see \n*/", true, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("seemissing") || hasWarning("see"));
  }

  @Test(timeout = 4000)
  public void testEmptyTemplateWarning() {
    JsDocInfoParser parser = createParser("@template \n*/", false, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("templatemissing") || hasWarning("template"));
  }

  @Test(timeout = 4000)
  public void testEmptyVersionWarning() {
    JsDocInfoParser parser = createParser("@version \n*/", false, null);
    assertTrue(parser.parse());
    assertTrue(hasWarning("versionmissing") || hasWarning("version"));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Standalone Type Parser Logic
  // =========================================================================

  @Test(timeout = 4000)
  public void testParseTypeStringPrimitives() {
    Node n1 = JsDocInfoParser.parseTypeString("number");
    assertNotNull(n1);
    assertEquals(Token.NAME, n1.getType());
    assertEquals("number", n1.getString());

    Node n2 = JsDocInfoParser.parseTypeString("null");
    assertNotNull(n2);
    assertEquals(Token.NAME, n2.getType());
    assertEquals("null", n2.getString());

    Node n3 = JsDocInfoParser.parseTypeString("undefined");
    assertNotNull(n3);
    assertEquals(Token.NAME, n3.getType());
    assertEquals("undefined", n3.getString());

    Node n4 = JsDocInfoParser.parseTypeString("*");
    assertNotNull(n4);
    assertEquals(Token.STAR, n4.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringModifiers() {
    Node qm = JsDocInfoParser.parseTypeString("?number");
    assertNotNull(qm);
    assertEquals(Token.QMARK, qm.getType());

    Node bang = JsDocInfoParser.parseTypeString("!number");
    assertNotNull(bang);
    assertEquals(Token.BANG, bang.getType());

    Node postfixQm = JsDocInfoParser.parseTypeString("number?");
    assertNotNull(postfixQm);
    assertEquals(Token.QMARK, postfixQm.getType());

    Node postfixBang = JsDocInfoParser.parseTypeString("number!");
    assertNotNull(postfixBang);
    assertEquals(Token.BANG, postfixBang.getType());

    Node onlyQm = JsDocInfoParser.parseTypeString("?");
    assertNotNull(onlyQm);
    assertEquals(Token.QMARK, onlyQm.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringUnion() {
    Node union1 = JsDocInfoParser.parseTypeString("(number|string)");
    assertNotNull(union1);
    assertEquals(Token.PIPE, union1.getType());

    Node union2 = JsDocInfoParser.parseTypeString("number|string");
    assertNotNull(union2);
    assertEquals(Token.PIPE, union2.getType());

    Node union3 = JsDocInfoParser.parseTypeString("number||string");
    assertNotNull(union3);
    assertEquals(Token.PIPE, union3.getType());

    Node union4 = JsDocInfoParser.parseTypeString("(number, string)");
    assertNotNull(union4);
    assertEquals(Token.PIPE, union4.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringRecord() {
    Node record = JsDocInfoParser.parseTypeString("{a: number, b: string}");
    assertNotNull(record);
    assertEquals(Token.LC, record.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringArray() {
    Node array = JsDocInfoParser.parseTypeString("[number, ...string]");
    assertNotNull(array);
    assertEquals(Token.LB, array.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringFunctionTypes() {
    Node f1 = JsDocInfoParser.parseTypeString("function(): void");
    assertNotNull(f1);
    assertEquals(Token.FUNCTION, f1.getType());

    Node f2 = JsDocInfoParser.parseTypeString("function(this:Object, number): boolean");
    assertNotNull(f2);
    assertEquals(Token.FUNCTION, f2.getType());

    Node f3 = JsDocInfoParser.parseTypeString("function(new:Object): void");
    assertNotNull(f3);
    assertEquals(Token.FUNCTION, f3.getType());

    Node f4 = JsDocInfoParser.parseTypeString("function(...[number]): void");
    assertNotNull(f4);
    assertEquals(Token.FUNCTION, f4.getType());

    Node f5 = JsDocInfoParser.parseTypeString("function(number=): void");
    assertNotNull(f5);
    assertEquals(Token.FUNCTION, f5.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringTypeApplication() {
    Node app = JsDocInfoParser.parseTypeString("Array.<string>");
    assertNotNull(app);
    assertEquals(Token.NAME, app.getType());
    assertEquals("Array", app.getString());
    assertTrue(app.hasChildren());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringInvalidSyntax() {
    assertNull(JsDocInfoParser.parseTypeString("!"));
    assertNull(JsDocInfoParser.parseTypeString("function("));
    assertNull(JsDocInfoParser.parseTypeString("function(this:)"));
    assertNull(JsDocInfoParser.parseTypeString("Array.<string"));
    assertNull(JsDocInfoParser.parseTypeString("{x:"));
  }

  @Test(timeout = 4000)
  public void testAssociatedNodeAttachment() {
    Node associated = IR.script();
    JsDocInfoParser parser = createParser("@type {string}*/", false, associated);
    assertTrue(parser.parse());
    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertEquals(info, associated.getJSDocInfo());
  }
}