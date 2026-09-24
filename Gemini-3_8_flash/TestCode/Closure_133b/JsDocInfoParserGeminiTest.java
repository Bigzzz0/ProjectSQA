/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.parsing.JsDocInfoParser
 * Target Defect: JsDocInfoParserTest::testTextExtents -> java.lang.IllegalStateException: Recorded bad position information
 *
 * Decision / Branch Matrix Covered:
 * 1. Defect-Targeted Zone:
 *    - Multiline text extraction where text block terminates at an annotation on the next/same line.
 *    - Extent calculations across header descriptions, @fileoverview, @author, and subsequent descriptions.
 * 2. Type Expression Grammar Branches (parseTypeString, parseTypeExpressionAnnotation, parseInlineTypeDoc):
 *    - Basic types (number, string, boolean, null, undefined, *, ?)
 *    - Nullable/Non-nullable prefixes and suffixes (?, !)
 *    - Unions: (A|B), A|B, backwards-compatible double pipes A||B
 *    - Generics & Type Applications: Array.<string>, Object.<string, number>
 *    - Record types: {a: number, b: string}, single-field, syntax errors
 *    - Function types: function(this:X, new:Y, string, ...[number]): boolean, void return, optional params
 *    - Array type syntax: [number, string], [...number]
 *    - Malformed syntax error recovery paths (missing '>', '}', ']', ')', ':', varargs order)
 * 3. Annotation Switch Matrix:
 *    - Descriptions & Metadata: @fileoverview, @author, @version, @see, @desc, @meaning, @deprecated
 *    - Structural & OOP: @constructor, @interface, @struct, @dict, @extends, @implements
 *    - Access & Scoping: @private, @protected, @public, @const, @define, @this, @typedef, @type
 *    - Flow & Optimization: @export, @expose, @externs, @noalias, @nocompile, @nocheck, @override, @nosideeffects
 *    - Contract & Attributes: @param (optional, rest, bracketed, dotted), @return, @throws, @modifies, @suppress, @template, @classTemplate
 * 4. Error Paths & Duplicate Checks:
 *    - Incompatible constructor + interface, struct + dict, duplicate extends/implements, duplicate suppressions/modifies/templates
 *    - Unexpected EOF handling, invalid tokens, unrecognized tags
 */

package com.google.javascript.jscomp.parsing;

import com.google.common.collect.Sets;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfo.Visibility;
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

public class JsDocInfoParserGeminiTest {

  private static class RecordingErrorReporter implements ErrorReporter {
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
      return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
    }
  }

  private Config createConfig(boolean parseDocumentation) {
    Set<String> extraAnnotations = Sets.newHashSet();
    Set<String> suppressions = Sets.newHashSet("checkTypes", "visibility", "deprecated");
    return new Config(
        extraAnnotations,
        suppressions,
        true,
        LanguageMode.ECMASCRIPT5,
        parseDocumentation);
  }

  private JsDocInfoParser createParser(String comment, boolean parseDocumentation, RecordingErrorReporter reporter) {
    Config config = createConfig(parseDocumentation);
    JsDocTokenStream stream = new JsDocTokenStream(comment);
    Node script = IR.script();
    return new JsDocInfoParser(stream, null, script, config, reporter);
  }

  private JSDocInfo parseSuccessfully(String comment) {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    JsDocInfoParser parser = createParser(comment, true, reporter);
    boolean success = parser.parse();
    assertTrue("Parser failed on comment: " + comment + " with errors: " + reporter.errors, success);
    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    if (info == null) {
      info = parser.getFileOverviewJSDocInfo();
    }
    return info;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J testTextExtents Defect)
  // =========================================================================

  @Test(timeout = 4000)
  public void testTextExtents() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    // Test multiline text ending right before another tag (known position extent bug)
    String jsdoc1 =
        "Some text with header\n" +
        " @author foo\n" +
        " * some more text\n" +
        " */";
    JsDocInfoParser parser1 = createParser(jsdoc1, true, reporter);
    parser1.parse();

    String jsdoc2 =
        "@fileoverview Some text with header\n" +
        " @author foo\n" +
        " * some more text\n" +
        " */";
    JsDocInfoParser parser2 = createParser(jsdoc2, true, reporter);
    parser2.parse();
    assertNotNull(parser2.getFileOverviewJSDocInfo());

    String jsdoc3 =
        "/**\n" +
        " * @fileoverview Some text with header\n" +
        " * @author foo\n" +
        " * some more text\n" +
        " */";
    JsDocInfoParser parser3 = createParser(jsdoc3, true, reporter);
    parser3.parse();
    assertNotNull(parser3.getFileOverviewJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testTextExtentsSingleLineAndParameterDescriptions() {
    // Tests extent recording across multiple adjacent tags and same-line descriptions
    String jsdoc =
        "/**\n" +
        " * @desc First line of description\n" +
        " * second line of description\n" +
        " * @param {string} p1 Param one description\n" +
        " *   continued on next line\n" +
        " * @return {boolean} Return description\n" +
        " * @deprecated Deprecated reason\n" +
        " *   extra reason details\n" +
        " * @throws {Error} Throws description\n" +
        " */";
    JSDocInfo info = parseSuccessfully(jsdoc);
    assertNotNull(info);
    assertEquals("First line of description second line of description", info.getDescription());
    assertEquals("Param one description continued on next line", info.getParameterDescription("p1"));
    assertEquals("Return description", info.getReturnDescription());
    assertEquals("Deprecated reason extra reason details", info.getDeprecationReason());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testParseInlineTypeDoc() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    JsDocInfoParser parser = createParser("{number}", false, reporter);
    JSDocInfo info = parser.parseInlineTypeDoc();
    assertNotNull(info);
    assertTrue(info.hasType());

    // Failing path
    JsDocInfoParser invalidParser = createParser("", false, reporter);
    JSDocInfo nullInfo = invalidParser.parseInlineTypeDoc();
    assertNull(nullInfo);
  }

  @Test(timeout = 4000)
  public void testParseTypeStringBasicAndPunctuation() {
    Node numberNode = JsDocInfoParser.parseTypeString("number");
    assertNotNull(numberNode);
    assertEquals(Token.STRING, numberNode.getType());
    assertEquals("number", numberNode.getString());

    Node starNode = JsDocInfoParser.parseTypeString("*");
    assertNotNull(starNode);
    assertEquals(Token.STAR, starNode.getType());

    Node qmarkNode = JsDocInfoParser.parseTypeString("?");
    assertNotNull(qmarkNode);
    assertEquals(Token.QMARK, qmarkNode.getType());

    Node nullNode = JsDocInfoParser.parseTypeString("null");
    assertNotNull(nullNode);
    assertEquals(Token.STRING, nullNode.getType());
    assertEquals("null", nullNode.getString());

    Node undefNode = JsDocInfoParser.parseTypeString("undefined");
    assertNotNull(undefNode);
    assertEquals(Token.STRING, undefNode.getType());
    assertEquals("undefined", undefNode.getString());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringModifiers() {
    Node notNull = JsDocInfoParser.parseTypeString("!Object");
    assertNotNull(notNull);
    assertEquals(Token.BANG, notNull.getType());
    assertEquals("Object", notNull.getFirstChild().getString());

    Node nullable = JsDocInfoParser.parseTypeString("?Object");
    assertNotNull(nullable);
    assertEquals(Token.QMARK, nullable.getType());
    assertEquals("Object", nullable.getFirstChild().getString());

    Node postBang = JsDocInfoParser.parseTypeString("Object!");
    assertNotNull(postBang);
    assertEquals(Token.BANG, postBang.getType());

    Node postQmark = JsDocInfoParser.parseTypeString("Object?");
    assertNotNull(postQmark);
    assertEquals(Token.QMARK, postQmark.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringUnionsAndPipes() {
    Node union = JsDocInfoParser.parseTypeString("(number|string)");
    assertNotNull(union);
    assertEquals(Token.PIPE, union.getType());
    assertEquals(2, union.getChildCount());

    Node topLevelUnion = JsDocInfoParser.parseTypeString("number|string");
    assertNotNull(topLevelUnion);
    assertEquals(Token.PIPE, topLevelUnion.getType());

    // Backwards compatibility with double pipe
    Node doublePipe = JsDocInfoParser.parseTypeString("number||string");
    assertNotNull(doublePipe);
    assertEquals(Token.PIPE, doublePipe.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringApplicationsAndRecords() {
    Node arrayApp = JsDocInfoParser.parseTypeString("Array.<string>");
    assertNotNull(arrayApp);
    assertEquals(Token.STRING, arrayApp.getType());
    assertEquals("Array", arrayApp.getString());
    assertTrue(arrayApp.hasChildren());

    Node record = JsDocInfoParser.parseTypeString("{foo: string, bar: number}");
    assertNotNull(record);
    assertEquals(Token.LC, record.getType());

    Node singleFieldRecord = JsDocInfoParser.parseTypeString("{foo}");
    assertNotNull(singleFieldRecord);
    assertEquals(Token.LC, singleFieldRecord.getType());

    Node arrayType = JsDocInfoParser.parseTypeString("[number, string]");
    assertNotNull(arrayType);
    assertEquals(Token.LB, arrayType.getType());

    Node arrayVarargs = JsDocInfoParser.parseTypeString("[...number]");
    assertNotNull(arrayVarargs);
    assertEquals(Token.LB, arrayVarargs.getType());
  }

  @Test(timeout = 4000)
  public void testParseTypeStringFunctionTypes() {
    Node fn = JsDocInfoParser.parseTypeString("function(this:Object, new:Array, string=, ...[number]): boolean");
    assertNotNull(fn);
    assertEquals(Token.FUNCTION, fn.getType());

    Node fnVoid = JsDocInfoParser.parseTypeString("function(): void");
    assertNotNull(fnVoid);
    assertEquals(Token.FUNCTION, fnVoid.getType());

    Node fnUntypedRest = JsDocInfoParser.parseTypeString("function(...)");
    assertNotNull(fnUntypedRest);
    assertEquals(Token.FUNCTION, fnUntypedRest.getType());
  }

  @Test(timeout = 4000)
  public void testAnnotationsConstructorAndInterface() {
    JSDocInfo ctor = parseSuccessfully("/** @constructor */");
    assertNotNull(ctor);
    assertTrue(ctor.isConstructor());

    JSDocInfo iface = parseSuccessfully("/** @interface */");
    assertNotNull(iface);
    assertTrue(iface.isInterface());

    RecordingErrorReporter reporter = new RecordingErrorReporter();
    JsDocInfoParser conflictParser1 = createParser("/** @constructor \n * @interface */", false, reporter);
    conflictParser1.parse();
    assertTrue(reporter.warnings.size() > 0);

    JsDocInfoParser conflictParser2 = createParser("/** @interface \n * @constructor */", false, reporter);
    conflictParser2.parse();
    assertTrue(reporter.warnings.size() > 0);
  }

  @Test(timeout = 4000)
  public void testAnnotationsStructAndDict() {
    JSDocInfo struct = parseSuccessfully("/** @struct */");
    assertNotNull(struct);
    assertTrue(struct.makesStructs());

    JSDocInfo dict = parseSuccessfully("/** @dict */");
    assertNotNull(dict);
    assertTrue(dict.makesDicts());

    RecordingErrorReporter reporter = new RecordingErrorReporter();
    JsDocInfoParser conflictParser = createParser("/** @struct \n * @dict */", false, reporter);
    conflictParser.parse();
    assertTrue(reporter.warnings.size() > 0);
  }

  @Test(timeout = 4000)
  public void testAnnotationsExtendsAndImplements() {
    JSDocInfo info = parseSuccessfully("/** @constructor \n * @extends {BaseClass} \n * @implements {MyInterface} */");
    assertNotNull(info);
    assertEquals(1, info.getImplementedInterfaceCount());
    assertEquals("MyInterface", info.getImplementedInterfaces().iterator().next().getItem().getString());
    assertEquals("BaseClass", info.getBaseType().getItem().getString());

    // Multiple extends on interface
    JSDocInfo ifaceMulti = parseSuccessfully("/** @interface \n * @extends {IOne} \n * @extends {ITwo} */");
    assertNotNull(ifaceMulti);
    assertEquals(2, ifaceMulti.getExtendedInterfacesCount());

    RecordingErrorReporter reporter = new RecordingErrorReporter();
    // Duplicate extends on constructor
    JsDocInfoParser duplicateExt = createParser("/** @constructor \n * @extends {A} \n * @extends {B} */", false, reporter);
    duplicateExt.parse();
    assertTrue(reporter.warnings.size() > 0);

    // Duplicate implements
    reporter.warnings.clear();
    JsDocInfoParser duplicateImp = createParser("/** @constructor \n * @implements {A} \n * @implements {A} */", false, reporter);
    duplicateImp.parse();
    assertTrue(reporter.warnings.size() > 0);
  }

  @Test(timeout = 4000)
  public void testAnnotationsVisibilityAndTypes() {
    JSDocInfo priv = parseSuccessfully("/** @private {number} */");
    assertNotNull(priv);
    assertEquals(Visibility.PRIVATE, priv.getVisibility());
    assertTrue(priv.hasType());

    JSDocInfo prot = parseSuccessfully("/** @protected */");
    assertNotNull(prot);
    assertEquals(Visibility.PROTECTED, prot.getVisibility());

    JSDocInfo pub = parseSuccessfully("/** @public */");
    assertNotNull(pub);
    assertEquals(Visibility.PUBLIC, pub.getVisibility());

    JSDocInfo cst = parseSuccessfully("/** @const {string} */");
    assertNotNull(cst);
    assertTrue(cst.isConstant());

    JSDocInfo def = parseSuccessfully("/** @define {boolean} */");
    assertNotNull(def);
    assertTrue(def.isDefine());

    JSDocInfo th = parseSuccessfully("/** @this {Object} */");
    assertNotNull(th);
    assertNotNull(th.getThisType());

    JSDocInfo tdef = parseSuccessfully("/** @typedef {string} */");
    assertNotNull(tdef);
    assertNotNull(tdef.getTypedefType());

    JSDocInfo en = parseSuccessfully("/** @enum {number} */");
    assertNotNull(en);
    assertTrue(en.hasEnumParameterType());
  }

  @Test(timeout = 4000)
  public void testAnnotationsParametersAndOptionality() {
    JSDocInfo info = parseSuccessfully(
        "/**\n" +
        " * @param {string} req Required\n" +
        " * @param {number=} opt Optional\n" +
        " * @param [bracketOpt] Bracketed\n" +
        " * @param [defaultOpt=10] Bracketed with default\n" +
        " * @param {...string} varargs Varargs\n" +
        " */");
    assertNotNull(info);
    assertTrue(info.hasParameter("req"));
    assertTrue(info.hasParameter("opt"));
    assertTrue(info.hasParameter("bracketOpt"));
    assertTrue(info.hasParameter("defaultOpt"));
    assertTrue(info.hasParameter("varargs"));
  }

  @Test(timeout = 4000)
  public void testAnnotationsSuppressionAndModifies() {
    JSDocInfo info = parseSuccessfully(
        "/**\n" +
        " * @param {Object} obj Target\n" +
        " * @suppress {checkTypes|visibility}\n" +
        " * @modifies {this|arguments|obj}\n" +
        " */");
    assertNotNull(info);
    Set<String> suppressions = info.getSuppressions();
    assertTrue(suppressions.contains("checkTypes"));
    assertTrue(suppressions.contains("visibility"));
    Set<String> modifies = info.getModifies();
    assertTrue(modifies.contains("this"));
    assertTrue(modifies.contains("arguments"));
    assertTrue(modifies.contains("obj"));
  }

  @Test(timeout = 4000)
  public void testAnnotationsTemplateAndClassTemplate() {
    JSDocInfo info = parseSuccessfully("/** @template T, V \n * @classTemplate C */");
    assertNotNull(info);
    assertEquals(2, info.getTemplateTypeNames().size());
    assertTrue(info.getTemplateTypeNames().contains("T"));
    assertTrue(info.getTemplateTypeNames().contains("V"));
    assertEquals(1, info.getClassTemplateTypeNames().size());
    assertTrue(info.getClassTemplateTypeNames().contains("C"));
  }

  @Test(timeout = 4000)
  public void testBooleanFlags() {
    JSDocInfo info = parseSuccessfully(
        "/**\n" +
        " * @ngInject\n" +
        " * @consistentIdGenerator\n" +
        " * @export\n" +
        " * @expose\n" +
        " * @externs\n" +
        " * @javadispatch\n" +
        " * @noalias\n" +
        " * @nocompile\n" +
        " * @nocheck\n" +
        " * @override\n" +
        " * @preserveTry\n" +
        " * @noshadow\n" +
        " * @nosideeffects\n" +
        " * @implicitCast\n" +
        " * @idGenerator\n" +
        " * @stableIdGenerator\n" +
        " */");
    assertNotNull(info);
    assertTrue(info.isNgInject());
    assertTrue(info.isConsistentIdGenerator());
    assertTrue(info.isExport());
    assertTrue(info.isExpose());
    assertTrue(info.isExterns());
    assertTrue(info.isJavaDispatch());
    assertTrue(info.isNoAlias());
    assertTrue(info.isNoCompile());
    assertTrue(info.isNoTypeCheck());
    assertTrue(info.isOverride());
    assertTrue(info.shouldPreserveTry());
    assertTrue(info.isNoShadow());
    assertTrue(info.hasNoSideEffects());
    assertTrue(info.isImplicitCast());
    assertTrue(info.isIdGenerator());
    assertTrue(info.isStableIdGenerator());
  }

  @Test(timeout = 4000)
  public void testLicenseAndPreserveOption() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    JsDocInfoParser parser = createParser(
        "/**\n" +
        " * @license MIT License\n" +
        " *   Copyright 2023\n" +
        " */",
        true, reporter);

    Node script = IR.script();
    Node.FileLevelJsDocBuilder fileLevelBuilder = script.getJsDocBuilderForNode();
    parser.setFileLevelJsDocBuilder(fileLevelBuilder);
    assertTrue(parser.parse());
    assertNotNull(parser.retrieveAndResetParsedJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testLendsAnnotation() {
    JSDocInfo info = parseSuccessfully("/** @lends {Namespace.prototype} */");
    assertNotNull(info);
    assertEquals("Namespace.prototype", info.getLendsName());

    JSDocInfo infoNoCurly = parseSuccessfully("/** @lends Namespace */");
    assertNotNull(infoNoCurly);
    assertEquals("Namespace", infoNoCurly.getLendsName());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyAndMinimalComments() {
    JSDocInfo empty = parseSuccessfully("/***/");
    assertNull(empty);

    JSDocInfo singleStar = parseSuccessfully("/** */");
    assertNull(singleStar);

    JSDocInfo multiLines = parseSuccessfully("/**\n *\n * \n */");
    assertNull(multiLines);
  }

  @Test(timeout = 4000)
  public void testParseTypeStringEdgeCases() {
    assertNull(JsDocInfoParser.parseTypeString(""));
    assertNull(JsDocInfoParser.parseTypeString("   "));
    assertNull(JsDocInfoParser.parseTypeString("?|number"));
    assertNull(JsDocInfoParser.parseTypeString("function(this)"));
    assertNull(JsDocInfoParser.parseTypeString("Array.<string"));
    assertNull(JsDocInfoParser.parseTypeString("[number"));
    assertNull(JsDocInfoParser.parseTypeString("(number"));
  }

  @Test(timeout = 4000)
  public void testDocumentationDisabledFlag() {
    // When documentation parsing is disabled, author/desc are suppressed/compacted
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    JsDocInfoParser parser = createParser(
        "/**\n" +
        " * Header block comment\n" +
        " * @author John\n" +
        " * @see Other\n" +
        " * @param {string} p Some description\n" +
        " * @return {number} Result description\n" +
        " */",
        false, reporter);

    assertTrue(parser.parse());
    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertEquals(0, info.getAuthors().size());
    assertNull(info.getParameterDescription("p"));
    assertNull(info.getReturnDescription());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testUnexpectedEOFHandling() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    // Missing closing '*/' triggers unexpected EOF
    JsDocInfoParser parser = createParser("/** @type {number}", false, reporter);
    boolean success = parser.parse();
    assertFalse(success);
    assertTrue(reporter.warnings.size() > 0);
  }

  @Test(timeout = 4000)
  public void testBadAnnotationTag() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    JsDocInfoParser parser = createParser("/** @thisTagDoesNotExist123 */", false, reporter);
    parser.parse();
    assertTrue(reporter.warnings.size() > 0);
  }

  @Test(timeout = 4000)
  public void testDuplicateAndIncompatibleWarnings() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();

    // Duplicate @desc
    JsDocInfoParser parserDesc = createParser("/** @desc First \n * @desc Second */", true, reporter);
    parserDesc.parse();
    assertTrue(reporter.warnings.size() > 0);

    // Duplicate @fileoverview
    reporter.warnings.clear();
    JsDocInfoParser parserFile = createParser("/** @fileoverview A \n * @fileoverview B */", true, reporter);
    parserFile.parse();
    assertTrue(reporter.warnings.size() > 0);

    // Duplicate @version
    reporter.warnings.clear();
    JsDocInfoParser parserVer = createParser("/** @version 1 \n * @version 2 */", true, reporter);
    parserVer.parse();
    assertTrue(reporter.warnings.size() > 0);

    // Duplicate @meaning
    reporter.warnings.clear();
    JsDocInfoParser parserMean = createParser("/** @meaning A \n * @meaning B */", true, reporter);
    parserMean.parse();
    assertTrue(reporter.warnings.size() > 0);

    // Duplicate @template
    reporter.warnings.clear();
    JsDocInfoParser parserTemp = createParser("/** @template T \n * @template V */", true, reporter);
    parserTemp.parse();
    assertTrue(reporter.warnings.size() > 0);

    // Duplicate parameter names
    reporter.warnings.clear();
    JsDocInfoParser parserParam = createParser("/** @param {string} a \n * @param {number} a */", true, reporter);
    parserParam.parse();
    assertTrue(reporter.warnings.size() > 0);

    // Missing parameter name
    reporter.warnings.clear();
    JsDocInfoParser parserParamNoName = createParser("/** @param {string} */", true, reporter);
    parserParamNoName.parse();
    assertTrue(reporter.warnings.size() > 0);

    // Unknown suppression
    reporter.warnings.clear();
    JsDocInfoParser parserSupp = createParser("/** @suppress {unknownSuppressionTag} */", false, reporter);
    parserSupp.parse();
    assertTrue(reporter.warnings.size() > 0);

    // Unknown modifies keyword
    reporter.warnings.clear();
    JsDocInfoParser parserMod = createParser("/** @modifies {unknownVariable} */", false, reporter);
    parserMod.parse();
    assertTrue(reporter.warnings.size() > 0);
  }

  @Test(timeout = 4000)
  public void testTypeSyntaxWarnings() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();

    // Missing right curly on extends
    JsDocInfoParser parserExt = createParser("/** @extends {Base */", false, reporter);
    parserExt.parse();
    assertTrue(reporter.warnings.size() > 0);

    // Missing right bracket on param
    reporter.warnings.clear();
    JsDocInfoParser parserBracket = createParser("/** @param [optParam */", false, reporter);
    parserBracket.parse();
    assertTrue(reporter.warnings.size() > 0);

    // Missing lends name
    reporter.warnings.clear();
    JsDocInfoParser parserLends = createParser("/** @lends */", false, reporter);
    parserLends.parse();
    assertTrue(reporter.warnings.size() > 0);

    // Missing template names
    reporter.warnings.clear();
    JsDocInfoParser parserTempEmpty = createParser("/** @template */", false, reporter);
    parserTempEmpty.parse();
    assertTrue(reporter.warnings.size() > 0);

    // Missing class template names
    reporter.warnings.clear();
    JsDocInfoParser parserClassTempEmpty = createParser("/** @classTemplate */", false, reporter);
    parserClassTempEmpty.parse();
    assertTrue(reporter.warnings.size() > 0);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & State Management
  // =========================================================================

  @Test(timeout = 4000)
  public void testSetAndGetFileOverviewJSDocInfo() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    JsDocInfoParser parser = createParser("/** @const */", false, reporter);

    assertNull(parser.getFileOverviewJSDocInfo());
    JSDocInfo dummy = new JSDocInfo();
    parser.setFileOverviewJSDocInfo(dummy);
    assertSame(dummy, parser.getFileOverviewJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testParamWithPropertySyntaxIgnored() {
    // Param names with dot (e.g. options.foo) should be quietly ignored as per JsDocToolkit compat
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    JsDocInfoParser parser = createParser(
        "/**\n" +
        " * @param {Object} options\n" +
        " * @param {string} options.name\n" +
        " */",
        true, reporter);
    assertTrue(parser.parse());
    JSDocInfo info = parser.retrieveAndResetParsedJSDocInfo();
    assertNotNull(info);
    assertTrue(info.hasParameter("options"));
    assertFalse(info.hasParameter("options.name"));
  }
}